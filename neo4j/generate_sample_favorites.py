from __future__ import annotations

from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "neo4j-export"


def read_csv_ids(path: Path, id_col_index: int = 0) -> list[int]:
    lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
    if not lines:
        return []
    rows = []
    for line in lines[1:]:
        if not line.strip():
            continue
        parts = [p.strip() for p in line.split(",")]
        if len(parts) <= id_col_index:
            continue
        try:
            rows.append(int(parts[id_col_index]))
        except ValueError:
            continue
    return rows


def main() -> None:
    users_path = OUT_DIR / "users.csv"
    products_path = OUT_DIR / "products.csv"
    favorites_path = OUT_DIR / "favorites.csv"

    if not users_path.exists() or not products_path.exists():
        raise SystemExit(
            "CSV base não encontrado. Gere primeiro com: python neo4j\\export_from_sql.py"
        )

    user_ids = read_csv_ids(users_path, 0)
    product_ids = read_csv_ids(products_path, 0)

    if not user_ids or not product_ids:
        raise SystemExit("Sem usuários ou produtos nos CSVs base.")

    at = datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")

    rows: list[tuple[int, int, str]] = []

    # Padrão para gerar pelo menos 1 recomendação (quando houver >=2 usuários e >=2 produtos)
    if len(user_ids) >= 2 and len(product_ids) >= 2:
        u0, u1 = user_ids[0], user_ids[1]
        p0, p1 = product_ids[0], product_ids[1]
        rows.append((u0, p0, at))
        rows.append((u1, p0, at))
        rows.append((u1, p1, at))

        # Opcional: reforça sinal com mais usuários favoritando p0
        for extra_u in user_ids[2:]:
            rows.append((extra_u, p0, at))

    else:
        # Caso mínimo: cada usuário favorita o primeiro produto
        p0 = product_ids[0]
        for u in user_ids:
            rows.append((u, p0, at))

    # Remove duplicatas mantendo ordem
    seen = set()
    dedup = []
    for u, p, t in rows:
        key = (u, p)
        if key in seen:
            continue
        seen.add(key)
        dedup.append((u, p, t))

    out_lines = ["userId,productId,at\n"]
    out_lines += [f"{u},{p},{t}\n" for (u, p, t) in dedup]
    favorites_path.write_text("".join(out_lines), encoding="utf-8")

    print("OK")
    print(f"favorites.csv: {len(dedup)} relações FAVORITED")


if __name__ == "__main__":
    main()
