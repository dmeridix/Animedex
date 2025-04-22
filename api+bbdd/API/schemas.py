from pydantic import BaseModel
from typing import List, Optional
import datetime

# ✅ Esquema para los géneros
class GenreResponse(BaseModel):
    id: int
    name: str

    class Config:
        from_attributes = True

# ✅ Esquema para la petición de añadir a favoritos
class FavCreate(BaseModel):
    id_anime: int
    status: str  # Puede ser "watching", "completed", etc.

# ✅ Esquema para la respuesta de favoritos (con fechas añadidas)
class FavResponse(BaseModel):
    id: int
    id_anime: int
    status: str
    date_added: Optional[datetime.datetime] = None
    date_finished: Optional[datetime.datetime] = None

    class Config:
        from_attributes = True


# Modelo intermedio para la respuesta del endpoint /anime/search
class AnimeWithStatusResponse(BaseModel):
    id: int
    title: str
    main_picture: Optional[str] = None
    num_episodes: Optional[int] = None
    status: str  # Estado del anime ("Planned", "Watching", "Completed" o "No Favorito")

    class Config:
        from_attributes = True

# ✅ Esquema para la respuesta del anime (sin date_added ni date_finished)
class AnimeResponse(BaseModel):
    id: int
    title: str
    main_picture: Optional[str] = None
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    synopsis: str
    mean: Optional[float] = None
    rank: Optional[int] = None
    popularity: Optional[int] = None
    media_type: str
    status: str
    num_episodes: Optional[int] = None
    start_season: Optional[str] = None
    genres: List[GenreResponse]

    class Config:
        from_attributes = True

    @classmethod
    def from_orm(cls, obj):
        return cls(
            id=obj.id,
            title=obj.title,
            main_picture=obj.main_picture,
            start_date=obj.start_date.strftime("%Y-%m-%d") if obj.start_date else None,  
            end_date=obj.end_date.strftime("%Y-%m-%d") if obj.end_date else None,        
            synopsis=obj.synopsis,
            mean=obj.mean,
            rank=obj.rank,
            popularity=obj.popularity,
            media_type=obj.media_type,
            status=obj.status,
            num_episodes=obj.num_episodes,
            start_season=obj.start_season,
            genres=[GenreResponse(id=g.id, name=g.name) for g in obj.genres]
        )