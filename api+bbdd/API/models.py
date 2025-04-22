from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
import datetime

Base = declarative_base()

class Anime(Base):
    __tablename__ = "anime"
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    main_picture = Column(String)
    start_date = Column(String, nullable=True)
    end_date = Column(String, nullable=True)
    synopsis = Column(String)
    mean = Column(Float, nullable=True)
    rank = Column(Integer, nullable=True)
    popularity = Column(Integer, nullable=True)
    media_type = Column(String)
    status = Column(String)
    num_episodes = Column(Integer, nullable=True)
    start_season = Column(String, nullable=True)
    genres = relationship("AnimeGenre", back_populates="anime")

class Genre(Base):
    __tablename__ = "genre"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)
    animes = relationship("AnimeGenre", back_populates="genre")

class AnimeGenre(Base):
    __tablename__ = "anime_genre"
    anime_id = Column(Integer, ForeignKey("anime.id"), primary_key=True)
    genre_id = Column(Integer, ForeignKey("genre.id"), primary_key=True)
    anime = relationship("Anime", back_populates="genres")
    genre = relationship("Genre", back_populates="animes")
    
class Fav(Base):
    __tablename__ = "favs"

    id = Column(Integer, primary_key=True, nullable=False, default=1)
    id_anime = Column(Integer, ForeignKey("anime.id"), primary_key=True, nullable=False)
    status = Column(String(20), nullable=False)
    date_added = Column(DateTime, nullable=True)  # 🔹 Se asigna automáticamente
    date_finished = Column(DateTime, nullable=True)  # Ahora en Fav

    anime = relationship("Anime", backref="favoritos")