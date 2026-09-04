from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, util
import pymysql
import re

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

    # 입력 문장 임베딩
    query_embedding = model.encode(
        normalized_name,
        convert_to_tensor=True
    )


    # 의미 유사도 계산
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

    results.sort(
        key=lambda x: x["score"],
        reverse=True
    )

    return {
        "originalName": original_name,
        "candidates": results[:3]
    }