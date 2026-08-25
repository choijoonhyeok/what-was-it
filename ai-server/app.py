from fastapi import FastAPI
from sentence_transformers import SentenceTransformer
from sentence_transformers.util import cos_sim

app = FastAPI()

model = SentenceTransformer("snunlp/KR-SBERT-V40K-klueNLI-augSTS")

CANDIDATES = [
    "문화누리카드",
    "문화누리바우처",
    "문화누리카드 온라인",
    "NICEPAY",
    "카카오페이",
    "네이버페이",
    "토스페이",
    "스타벅스",
    "CU",
    "GS25"
]


@app.get("/ai/candidates")
def get_candidates(merchant_name: str):

    query_embedding = model.encode(
        merchant_name,
        convert_to_tensor=True
    )

    candidate_embeddings = model.encode(
        CANDIDATES,
        convert_to_tensor=True
    )

    similarities = cos_sim(
        query_embedding,
        candidate_embeddings
    )[0]

    results = []

    for candidate, score in zip(CANDIDATES, similarities):
        results.append({
            "name": candidate,
            "score": float(score)
        })

    results.sort(
        key=lambda x: x["score"],
        reverse=True
    )

    return {
        "originalName": merchant_name,
        "candidates": results[:3]
    }