from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# URL de conexión con SQLAlchemy
DATABASE_URL = "mysql+pymysql://dani:1234567890@mariadb/anime_db"

# Crear el motor de la base de datos
engine = create_engine(DATABASE_URL, pool_size=5, max_overflow=10)

# Crear la sesión de la BD
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Dependencia para obtener la sesión
def get_db():
    db = SessionLocal()
    try:
        yield db  # Permite inyectar la sesión en los endpoints
    finally:
        db.close()