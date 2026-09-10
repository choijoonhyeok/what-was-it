from dotenv import load_dotenv
from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, util
import pymysql
import re
import requests
import os

load_dotenv()

app = FastAPI()

def normalize_merchant_name(name):

    # 1. 앞뒤 공백 제거
    name = name.strip()

    # 2. 여러 개의 공백을 하나로 변경
    name = re.sub(r"\s+", " ", name)

    # 3. 일부 구분 기호를 공백으로 변경
    name = re.sub(r"[-_/#]", " ", name)

    # 4. 다시 연속된 공백 정리
    name = re.sub(r"\s+", " ", name).strip()

    return name

def search_naver(merchant_name):
    client_id = os.getenv("NCP_APIGW_API_KEY_ID")
    client_secret = os.getenv("NCP_APIGW_API_KEY")

    print("현재 Python:", os.sys.executable)
    print("NCP_APIGW_API_KEY_ID:", client_id)
    print("NCP_APIGW_API_KEY 존재:", bool(client_secret))

    url = "https://naverapihub.apigw.ntruss.com/search/v1/webkr"

    headers = {
       "X-NCP-APIGW-API-KEY-ID": client_id,
       "X-NCP-APIGW-API-KEY": client_secret
    }

    params = {
        "query": merchant_name,
        "display": 5,
        "start": 1,
        "sort": "random",
        "format": "json"
    }

    response = requests.get(
        url,
        headers=headers,
        params=params
        )

    print("Naver status:", response.status_code)
    print("Naver response:", response.text)

    response.raise_for_status()

    data = response.json()

    results = []

    for item in data.get("items", []):
        title = re.sub(r"<[^>]+>", "", item.get("title", ""))
        description = re.sub(r"<[^>]+>", "", item.get("description", ""))

        results.append({
            "title": title,
            "link": item.get("link", ""),
            "description": description
        })

    results = sort_search_results(results)

    return {
        "merchantName": merchant_name,
        "results": results
    }

# AI 모델
model = SentenceTransformer("snunlp/KR-SBERT-V40K-klueNLI-augSTS")


# DB 연결
def get_connection():
    return pymysql.connect(
        host="localhost",
        user="root",
        password="1234",
        database="what_was_it",
        charset="utf8mb4"
    )


# DB에서 후보 가맹점 조회
def get_candidates():

    print("===== DB 조회 시작 =====")

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            sql = """
                SELECT id, merchant_name, category, description
                FROM merchant_candidate
            """

            cursor.execute(sql)
            candidates = cursor.fetchall()

            print("DB 후보 개수:", len(candidates))
            print("DB 후보:", candidates)

            return candidates

        

    finally:
        connection.close()


# 서버 시작 시 후보 데이터와 임베딩 생성
candidates = get_candidates()

candidate_names = [
    candidate[1]
    for candidate in candidates
]

candidate_embeddings = model.encode(
    candidate_names,
    convert_to_tensor=True
)



# 요청 데이터 형식
class MerchantRequest(BaseModel):
    merchant_name: str


# 결제 가맹점명 분석
@app.post("/analyze")
def analyze(request: MerchantRequest):

    print("===== ANALYZE START =====")
    print("입력값:", request.merchant_name)
    
    original_name = request.merchant_name
    normalized_name = normalize_merchant_name(original_name)

    print("전처리값:", normalized_name)

    # 1. 입력값 임베딩
    query_embedding = model.encode(
        normalized_name,
        convert_to_tensor=True
    )

    # 2. 후보 DB와 유사도 계산
    similarities = util.cos_sim(
        query_embedding,
        candidate_embeddings
    )[0]

    # 점수 순으로 정렬
    results = []

    for index, score in enumerate(similarities):
        candidate = candidates[index]

        results.append({
            "id": candidate[0],
            "name": candidate[1],
            "category": candidate[2],
            "description": candidate[3],
            "score": float(score)
        })

    # 3. 유사도 기준 Top 3
    results.sort(
        key=lambda x: x["score"],
        reverse=True
    )

    top_candidates = results[:3]

    print("===== TOP 3 후보 =====")
    for candidate in top_candidates:
        print(
            candidate["name"],
            candidate["score"]
        )
     # 4. Top 3 후보 각각 네이버 검색
    candidate_evidences = []

    for candidate in top_candidates:

        search_results = search_naver(
            candidate["name"]
        )["results"]

        rag_context = create_rag_context(
            search_results
        )

        candidate_evidences.append({
            "candidate": candidate,
            "searchResults": search_results,
            "ragContext": rag_context
        })



    return {
        "originalName": original_name,
        "normalizedName": normalized_name,
        "candidates": candidate_evidences
        
    }

class SerachRequest(BaseModel):
    merchant_name: str

@app.post("/search")
def search(request: SerachRequest):

    merchant_name = normalize_merchant_name(request.merchant_name)

    search_results = search_naver(merchant_name)

    results = search_results["results"]

    rag_context = create_rag_context(results)

    return{
        "merchantName": merchant_name,
        "results": results,
        "ragContext": rag_context
    }

def sort_search_results(results):
    def get_score(result):
        link = result.get("link", "").lower()

        # 신뢰도가 낮은 정보 사이트는 후순위

        if "namu.wiki" in link:
            return 4

        if "wikipedia.org" in link:
            return 4

        # 앱스토어는 공식 서비스 정보 확인에 유용
        if "play.google.com" in link:
            return 2

        if "apps.apple.com" in link:
            return 2

        # 그 외 검색 결과
        return 1


    return sorted(results, key=get_score)

def create_rag_context(results):
    context = []

    for i, result in enumerate(results, start=1):
        context.append(
            f"[근거 {i}]\n"
            f"제목: {result['title']}\n"
            f"설명: {result['description']}\n"
            f"출처: {result['link']}"
        )

    return "\n\n".join(context)