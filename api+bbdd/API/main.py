from fastapi import FastAPI, HTTPException, Depends
from sqlalchemy.orm import Session
import requests
import datetime
import asyncio
from API.models import Anime, Genre, AnimeGenre, Fav
from API.schemas import AnimeResponse, GenreResponse, FavResponse, AnimeWithStatusResponse
from API.db import SessionLocal
from typing import List
from sqlalchemy import func


app = FastAPI()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        
@app.post("/favorites/", status_code=201)
def add_favorite(id_anime: int, status: str = "Planned", db: Session = Depends(get_db)):
    """
    Añade un anime a favoritos usando su ID.
    """
    user_id = 1  # 🔥 Usuario fijo por ahora

    # Verificar si el anime ya está en favoritos
    existing_fav = db.query(Fav).filter(
        Fav.id == user_id,
        Fav.id_anime == id_anime
    ).first()
    if existing_fav:
        raise HTTPException(status_code=400, detail="El anime ya está en favoritos")

    # Crear un nuevo registro en la tabla `Fav`
    new_fav = Fav(
        id=user_id,  # 🔹 Usar `id` en lugar de `id_usuario`
        id_anime=id_anime,
        status=status,
        date_added=datetime.datetime.utcnow(),  # 🔹 Fecha actual al agregar
        date_finished=None  # 🔹 Inicialmente None
    )

    # Guardar el nuevo registro en la base de datos
    db.add(new_fav)
    db.commit()

    return {"message": "Anime añadido a favoritos", "date_added": new_fav.date_added}

@app.get("/favorites/", response_model=List[FavResponse])
def get_favorites(db: Session = Depends(get_db)):
    user_id = 1  # 🔥 Usuario fijo por ahora

    # 🔹 Cambio: Usar Fav.id en lugar de Fav.id_usuario
    favorites = db.query(Fav).filter(Fav.id == user_id).all()

    return favorites  # 🔥 Ahora incluye `date_finished` en la respuesta

@app.put("/favorites/{id_anime}/status", status_code=200)
def update_favorite_status(id_anime: int, status: str, db: Session = Depends(get_db)):
    user_id = 1  # 🔥 Usuario fijo por ahora

    # 🔹 Cambio: Usar Fav.id en lugar de Fav.id_usuario
    favorite = db.query(Fav).filter(Fav.id == user_id, Fav.id_anime == id_anime).first()

    if not favorite:
        raise HTTPException(status_code=404, detail="El anime no está en favoritos")

    favorite.status = status

    # 🔹 Si el estado es "Completed", se asigna `date_finished`
    if status.lower() == "Watched":
        favorite.date_finished = datetime.datetime.utcnow()
    else:
        favorite.date_finished = None  # 🔹 Se borra si no es "Completed"

    db.commit()

    return {"message": "Estado de favorito actualizado", "status": favorite.status, "date_finished": favorite.date_finished}

from fastapi import HTTPException, Depends, status
from sqlalchemy.orm import Session



@app.delete("/favorites/{id_anime}", status_code=status.HTTP_200_OK)
def remove_favorite(id_anime: int, db: Session = Depends(get_db)):


    user_id = 1  # 🔥 Usuario fijo por ahora

    # Eliminar el registro directamente en la consulta
    deleted_count = db.query(Fav).filter(
        Fav.id == user_id,
        Fav.id_anime == id_anime
    ).delete()

    # Si no se eliminó ningún registro, lanzar un error
    if deleted_count == 0:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="El anime no está en favoritos"
        )

    # Confirmar los cambios en la base de datos
    db.commit()

    return {"message": "Anime eliminado de favoritos"}



# 🔹 Función para obtener el anime desde la API de MyAnimeList si no está en la base de datos
def fetchAnimeInfo(anime_id):
    url = f"https://api.myanimelist.net/v2/anime/{anime_id}?fields=id,title,main_picture,start_date,end_date,synopsis,mean,rank,popularity,media_type,status,num_episodes,start_season,genres"
    
    headers = {
        "X-MAL-CLIENT-ID": "389af6d7882d7b2315de49882a8d8451"  # Reemplaza con tu Client ID
    }

    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        return response.json()
    else:
        print(f"Error {response.status_code}: {response.text}")
        return None

@app.get("/anime/popular", response_model=list[AnimeResponse])
def get_popular_anime(db: Session = Depends(get_db)):
    url = "https://api.myanimelist.net/v2/anime/ranking"
    params = {
        "fields": "id,title,main_picture,start_date,end_date,synopsis,mean,rank,popularity,media_type,status,num_episodes,start_season,genres",
        "ranking_type": "bypopularity",
        "limit": 20
    }
    headers = {"X-MAL-CLIENT-ID": "389af6d7882d7b2315de49882a8d8451"}
    response = requests.get(url, headers=headers, params=params)
    
    if response.status_code != 200:
        raise HTTPException(status_code=response.status_code, detail="Error al obtener datos de MyAnimeList")

    anime_list = response.json().get("data", [])
    results = []
    
    for item in anime_list:
        anime_data = item["node"]
        anime = db.query(Anime).filter(Anime.id == anime_data["id"]).first()
        
        if not anime:
            start_season = anime_data.get("start_season")
            start_season_str = f"{start_season['season']} {start_season['year']}" if start_season else None
            
            anime = Anime(
                id=anime_data["id"],
                title=anime_data["title"],
                main_picture=anime_data["main_picture"]["medium"] if anime_data.get("main_picture") else None,
                start_date=anime_data.get("start_date"),
                end_date=anime_data.get("end_date"),
                synopsis=anime_data.get("synopsis", ""),
                mean=anime_data.get("mean"),
                rank=anime_data.get("rank"),
                popularity=anime_data.get("popularity"),
                media_type=anime_data.get("media_type", ""),
                status=anime_data.get("status", ""),
                num_episodes=anime_data.get("num_episodes"),
                start_season=start_season_str
            )
            db.add(anime)
            db.commit()
            db.refresh(anime)
            
            for genre_data in anime_data.get("genres", []):
                genre = db.query(Genre).filter(Genre.id == genre_data["id"]).first()
                if not genre:
                    genre = Genre(id=genre_data["id"], name=genre_data["name"])
                    db.add(genre)
                    db.commit()
                    db.refresh(genre)
                
                anime_genre = AnimeGenre(anime_id=anime.id, genre_id=genre.id)
                db.add(anime_genre)
            
            db.commit()
        
        genres = (
            db.query(Genre.id, Genre.name)
            .join(AnimeGenre, Genre.id == AnimeGenre.genre_id)
            .filter(AnimeGenre.anime_id == anime.id)
            .all()
        )
        genre_list = [GenreResponse(id=g.id, name=g.name) for g in genres]
        
        results.append(AnimeResponse(
            id=anime.id,
            title=anime.title,
            main_picture=anime.main_picture,
            start_date=anime.start_date.strftime("%Y-%m-%d") if anime.start_date else None,
            end_date=anime.end_date.strftime("%Y-%m-%d") if anime.end_date else None,
            synopsis=anime.synopsis,
            mean=anime.mean,
            rank=anime.rank,
            popularity=anime.popularity,
            media_type=anime.media_type,
            status=anime.status,
            num_episodes=anime.num_episodes,
            start_season=anime.start_season,
            genres=genre_list
        ))
    
    return results


@app.get("/anime/search", response_model=List[AnimeWithStatusResponse])
def search_animes(q: str, db: Session = Depends(get_db)):
    user_id = 1  # 🔹 Usuario fijo por ahora
    animes = (
        db.query(
            Anime.id,
            Anime.title,
            Anime.main_picture,
            Anime.num_episodes,
            func.coalesce(Fav.status, "No Favorito").label("status")  # Si no está en favs, poner "No Favorito"
        )
        .outerjoin(Fav, (Anime.id == Fav.id_anime) & (Fav.id == user_id))  # 🔹 Usamos Fav.id en lugar de Fav.id_usuario
        .filter(Anime.title.ilike(f"%{q}%"))  # 🔹 Búsqueda insensible a mayúsculas/minúsculas
        .all()
    )
    return [
        {
            "id": a.id,
            "title": a.title,
            "main_picture": a.main_picture,
            "num_episodes": a.num_episodes,
            "status": a.status
        }
        for a in animes
    ]
    
@app.get("/favorites/search", response_model=List[FavResponse])
def search_favorites(q: str, db: Session = Depends(get_db)):
    user_id = 1  # 🔹 Usuario fijo por ahora
    favorites = (
        db.query(Fav)
        .join(Anime, Fav.id_anime == Anime.id)  # 🔹 Relacionamos `Fav` con `Anime`
        .filter(Fav.id == user_id, Anime.title.ilike(f"%{q}%"))  # 🔹 Filtramos por título del anime
        .all()
    )
    return favorites

@app.get("/anime/airing", response_model=list[AnimeResponse])
def get_airing_anime(db: Session = Depends(get_db)):
    """ 🔹 Obtiene la lista de animes que están actualmente en emisión desde MyAnimeList """
    
    url = "https://api.myanimelist.net/v2/anime/ranking"
    params = {
        "fields": "id,title,main_picture,start_date,end_date,synopsis,mean,rank,popularity,media_type,status,num_episodes,start_season,genres",
        "ranking_type": "airing",
        "limit": 20  # 🔹 Puedes cambiar el límite si lo deseas
    }
    
    headers = {
        "X-MAL-CLIENT-ID": "389af6d7882d7b2315de49882a8d8451"
    }

    response = requests.get(url, headers=headers, params=params)

    if response.status_code != 200:
        raise HTTPException(status_code=response.status_code, detail="Error al obtener datos de MyAnimeList")

    anime_list = response.json().get("data", [])
    results = []

    for item in anime_list:
        anime_data = item["node"]
        anime = db.query(Anime).filter(Anime.id == anime_data["id"]).first()
        
        if not anime:
            start_season = anime_data.get("start_season")
            start_season_str = f"{start_season['season']} {start_season['year']}" if start_season else None

            anime = Anime(
                id=anime_data["id"],
                title=anime_data["title"],
                main_picture=anime_data["main_picture"]["medium"] if anime_data.get("main_picture") else None,
                start_date=anime_data.get("start_date"),
                end_date=anime_data.get("end_date"),
                synopsis=anime_data.get("synopsis", ""),
                mean=anime_data.get("mean"),
                rank=anime_data.get("rank"),
                popularity=anime_data.get("popularity"),
                media_type=anime_data.get("media_type", ""),
                status=anime_data.get("status", ""),
                num_episodes=anime_data.get("num_episodes"),
                start_season=start_season_str
            )

            db.add(anime)
            db.commit()
            db.refresh(anime)

            for genre_data in anime_data.get("genres", []):
                genre = db.query(Genre).filter(Genre.id == genre_data["id"]).first()
                if not genre:
                    genre = Genre(id=genre_data["id"], name=genre_data["name"])
                    db.add(genre)
                    db.commit()
                    db.refresh(genre)
                
                anime_genre = AnimeGenre(anime_id=anime.id, genre_id=genre.id)
                db.add(anime_genre)

            db.commit()

        genres = (
            db.query(Genre.id, Genre.name)
            .join(AnimeGenre, Genre.id == AnimeGenre.genre_id)
            .filter(AnimeGenre.anime_id == anime.id)
            .all()
        )
        genre_list = [GenreResponse(id=g.id, name=g.name) for g in genres]

        results.append(AnimeResponse(
            id=anime.id,
            title=anime.title,
            main_picture=anime.main_picture,
            start_date=anime.start_date.strftime("%Y-%m-%d") if anime.start_date else None,
            end_date=anime.end_date.strftime("%Y-%m-%d") if anime.end_date else None,
            synopsis=anime.synopsis,
            mean=anime.mean,
            rank=anime.rank,
            popularity=anime.popularity,
            media_type=anime.media_type,
            status=anime.status,
            num_episodes=anime.num_episodes,
            start_season=anime.start_season,
            genres=genre_list
        ))

    return results

from random import sample
from sqlalchemy.sql.expression import func

@app.get("/anime/random", response_model=List[AnimeResponse])
def get_random_animes(db: Session = Depends(get_db)):
    """
    Devuelve 100 animes aleatorios de la base de datos.
    """
    # Obtener el total de animes en la base de datos
    total_animes = db.query(Anime).count()
    if total_animes == 0:
        raise HTTPException(status_code=404, detail="No hay animes en la base de datos")

    # Seleccionar 100 IDs aleatorios (o menos si hay menos de 100 animes)
    random_ids = sample(range(1, total_animes + 1), min(100, total_animes))

    # Consultar los animes correspondientes a esos IDs
    random_animes = db.query(Anime).filter(Anime.id.in_(random_ids)).all()

    # Construir la respuesta con los géneros relacionados
    results = []
    for anime in random_animes:
        genres = (
            db.query(Genre.id, Genre.name)
            .join(AnimeGenre, Genre.id == AnimeGenre.genre_id)
            .filter(AnimeGenre.anime_id == anime.id)
            .all()
        )
        genre_list = [GenreResponse(id=g.id, name=g.name) for g in genres]
        results.append(
            AnimeResponse(
                id=anime.id,
                title=anime.title,
                main_picture=anime.main_picture,
                start_date=anime.start_date.strftime("%Y-%m-%d") if anime.start_date else None,
                end_date=anime.end_date.strftime("%Y-%m-%d") if anime.end_date else None,
                synopsis=anime.synopsis,
                mean=anime.mean,
                rank=anime.rank,
                popularity=anime.popularity,
                media_type=anime.media_type,
                status=anime.status,
                num_episodes=anime.num_episodes,
                start_season=anime.start_season,
                genres=genre_list
            )
        )

    return results

@app.get("/anime/{anime_id}", response_model=AnimeResponse)
def get_anime(anime_id: int, db: Session = Depends(get_db)):
    # 🔹 Buscar en la base de datos
    anime = db.query(Anime).filter(Anime.id == anime_id).first()

    if not anime:
        # 🔹 Si no existe, buscar en la API de MyAnimeList
        anime_data = fetchAnimeInfo(anime_id)
        if not anime_data:
            raise HTTPException(status_code=404, detail="Anime not found")

        # 🔹 Crear nuevo objeto Anime
        start_season = anime_data.get("start_season")
        start_season_str = f"{start_season['season']} {start_season['year']}" if start_season else None

        new_anime = Anime(
            id=anime_data["id"],
            title=anime_data["title"],
            main_picture=anime_data["main_picture"]["medium"] if anime_data.get("main_picture") else None,
            start_date=anime_data.get("start_date"),
            end_date=anime_data.get("end_date"),
            synopsis=anime_data.get("synopsis", ""),
            mean=anime_data.get("mean"),
            rank=anime_data.get("rank"),
            popularity=anime_data.get("popularity"),
            media_type=anime_data.get("media_type", ""),
            status=anime_data.get("status", ""),
            num_episodes=anime_data.get("num_episodes"),
            start_season=start_season_str  # 🔹 Convertirlo en string
        )
        
        db.add(new_anime)
        db.commit()
        db.refresh(new_anime)

        # 🔹 Procesar los géneros
        genre_list = []
        for genre_data in anime_data.get("genres", []):
            genre = db.query(Genre).filter(Genre.id == genre_data["id"]).first()
            if not genre:
                genre = Genre(id=genre_data["id"], name=genre_data["name"])
                db.add(genre)
                db.commit()
                db.refresh(genre)
            
            # 🔹 Relacionar el anime con el género
            anime_genre = AnimeGenre(anime_id=new_anime.id, genre_id=genre.id)
            db.add(anime_genre)
            genre_list.append(GenreResponse(id=genre.id, name=genre.name))

        db.commit()
        
        # 🔹 Retornar la respuesta del anime nuevo con las fechas convertidas correctamente
        return AnimeResponse(
            id=new_anime.id,
            title=new_anime.title,
            main_picture=new_anime.main_picture,
            # 🔹 Si la fecha ya es string, se deja igual; si es datetime, se convierte a string con formato "YYYY-MM-DD"
            start_date=new_anime.start_date if isinstance(new_anime.start_date, str) else (new_anime.start_date.strftime("%Y-%m-%d") if new_anime.start_date else None),
            end_date=new_anime.end_date if isinstance(new_anime.end_date, str) else (new_anime.end_date.strftime("%Y-%m-%d") if new_anime.end_date else None),
            synopsis=new_anime.synopsis,
            mean=new_anime.mean,
            rank=new_anime.rank,
            popularity=new_anime.popularity,
            media_type=new_anime.media_type,
            status=new_anime.status,
            num_episodes=new_anime.num_episodes,
            start_season=new_anime.start_season,
            genres=genre_list
        )

    # 🔹 Si el anime ya está en la base de datos, obtener géneros
    genres = (
        db.query(Genre.id, Genre.name)
        .join(AnimeGenre, Genre.id == AnimeGenre.genre_id)
        .filter(AnimeGenre.anime_id == anime_id)
        .all()
    )
    genre_list = [GenreResponse(id=genre.id, name=genre.name) for genre in genres]

    # 🔹 Retornar el anime desde la base de datos con las fechas convertidas correctamente
    return AnimeResponse(
        id=anime.id,
        title=anime.title,
        main_picture=anime.main_picture,
        # 🔹 Convertir start_date y end_date de datetime a string si es necesario
        start_date=anime.start_date.strftime("%Y-%m-%d") if anime.start_date else None,
        end_date=anime.end_date.strftime("%Y-%m-%d") if anime.end_date else None,
        synopsis=anime.synopsis,
        mean=anime.mean,
        rank=anime.rank,
        popularity=anime.popularity,
        media_type=anime.media_type,
        status=anime.status,
        num_episodes=anime.num_episodes,
        start_season=anime.start_season,
        genres=genre_list
    )

    
# 🔹 Nuevo Endpoint para poblar la base de datos con 200 animes
@app.post("/populate_anime")
async def populate_anime(db: Session = Depends(get_db)):
    """ 🔹 Endpoint para llenar la base de datos con 200 animes nuevos """
    added_count = 0  # Contador de animes añadidos
    anime_id = 1  # Comenzamos desde el ID 1

    while added_count < 200:
        existing_anime = db.query(Anime).filter(Anime.id == anime_id).first()

        if not existing_anime:  # 🔹 Solo si el anime no existe en la BD
            anime_data = fetchAnimeInfo(anime_id)

            if anime_data:  # 🔹 Si se obtuvo información válida
                start_season = anime_data.get("start_season")
                start_season_str = f"{start_season['season']} {start_season['year']}" if start_season else None

                new_anime = Anime(
                    id=anime_data["id"],
                    title=anime_data["title"],
                    main_picture=anime_data["main_picture"]["medium"] if anime_data.get("main_picture") else None,
                    start_date=anime_data.get("start_date"),
                    end_date=anime_data.get("end_date"),
                    synopsis=anime_data.get("synopsis", ""),
                    mean=anime_data.get("mean"),
                    rank=anime_data.get("rank"),
                    popularity=anime_data.get("popularity"),
                    media_type=anime_data.get("media_type", ""),
                    status=anime_data.get("status", ""),
                    num_episodes=anime_data.get("num_episodes"),
                    start_season=start_season_str
                )

                db.add(new_anime)
                db.commit()
                db.refresh(new_anime)
                added_count += 1  # 🔹 Incrementamos el contador de animes añadidos

                # 🔹 Procesar y agregar los géneros
                for genre_data in anime_data.get("genres", []):
                    # Buscar o crear el género
                    genre = db.query(Genre).filter(Genre.id == genre_data["id"]).first()
                    if not genre:
                        genre = Genre(id=genre_data["id"], name=genre_data["name"])
                        db.add(genre)
                        db.commit()  # Guardamos inmediatamente para evitar problemas de FK
                        db.refresh(genre)

                    # Verificar si ya existe la relación anime-genre antes de insertarla
                    existing_anime_genre = (
                        db.query(AnimeGenre)
                        .filter_by(anime_id=new_anime.id, genre_id=genre.id)
                        .first()
                    )

                    if not existing_anime_genre:
                        anime_genre = AnimeGenre(anime_id=new_anime.id, genre_id=genre.id)
                        db.add(anime_genre)

                db.commit()  # 🔹 Commit final para guardar todas las relaciones

        anime_id += 1  # 🔹 Pasamos al siguiente ID de anime
        await asyncio.sleep(1)  # 🔹 Espera de 1s para no saturar la API

    return {"message": f"Se añadieron {added_count} animes nuevos a la base de datos."}

@app.put("/favorites/{id_anime}/planned", status_code=200)
def mark_as_planned(id_anime: int, db: Session = Depends(get_db)):
    user_id = 1  # 🔥 Usuario fijo por ahora

    favorite = db.query(Fav).filter(Fav.id == user_id, Fav.id_anime == id_anime).first()

    if not favorite:
        raise HTTPException(status_code=404, detail="El anime no está en favoritos")

    favorite.status = "Planned"
    favorite.date_finished = None  # 🔹 Se borra si no es "Completed"
    favorite.date_added = None  
    db.commit()

    return {"message": "Anime marcado como 'Planned'", "status": favorite.status}

@app.put("/favorites/{id_anime}/watching", status_code=200)
def mark_as_watching(id_anime: int, db: Session = Depends(get_db)):
    user_id = 1  # 🔥 Usuario fijo por ahora

    favorite = db.query(Fav).filter(Fav.id == user_id, Fav.id_anime == id_anime).first()

    if not favorite:
        raise HTTPException(status_code=404, detail="El anime no está en favoritos")

    favorite.status = "Watching"
    favorite.date_finished = None  # 🔹 Se borra si no es "Completed"
    favorite.date_added = datetime.datetime.utcnow() 
    db.commit()

    return {"message": "Anime marcado como 'Watching'", "status": favorite.status}

@app.put("/favorites/{id_anime}/completed", status_code=200)
def mark_as_completed(id_anime: int, db: Session = Depends(get_db)):
    user_id = 1  # 🔥 Usuario fijo por ahora

    favorite = db.query(Fav).filter(Fav.id == user_id, Fav.id_anime == id_anime).first()

    if not favorite:
        raise HTTPException(status_code=404, detail="El anime no está en favoritos")

    favorite.status = "Watched"
    
    # 🔹 Si date_added es NULL, asignar la fecha actual
    if favorite.date_added is None:
        favorite.date_added = datetime.datetime.utcnow()
    
    # 🔹 Asignar fecha actual a date_finished
    favorite.date_finished = datetime.datetime.utcnow()
    
    db.commit()

    return {"message": "Anime marcado como 'Watched'", "status": favorite.status, "date_added": favorite.date_added, "date_finished": favorite.date_finished}

# Get de los estados de los animes favoritos

@app.get("/favorites/planned", response_model=List[FavResponse])
def get_planned_favorites(db: Session = Depends(get_db)):
    user_id = 1  # 🔥 Usuario fijo por ahora
    planned_favorites = db.query(Fav).filter(Fav.id == user_id, Fav.status == "Planned").all()
    return planned_favorites

@app.get("/favorites/watching", response_model=List[FavResponse])
def get_watching_favorites(db: Session = Depends(get_db)):
    user_id = 1  # 🔥 Usuario fijo por ahora
    watching_favorites = db.query(Fav).filter(Fav.id == user_id, Fav.status == "Watching").all()
    return watching_favorites

@app.get("/favorites/completed", response_model=List[FavResponse])
def get_completed_favorites(db: Session = Depends(get_db)):
    user_id = 1  # 🔥 Usuario fijo por ahora
    completed_favorites = db.query(Fav).filter(Fav.id == user_id, Fav.status == "Watched").all()
    return completed_favorites


@app.post("/populatePopularity")
async def populate_popularity(db: Session = Depends(get_db)):
    """
    🔹 Endpoint para poblar la base de datos con 200 animes populares.
    🔹 Los animes se obtienen de MyAnimeList ordenados por popularidad.
    """
    added_count = 0  # Contador de animes añadidos
    offset = 0  # Offset para paginación en la API de MyAnimeList

    while added_count < 200:
        # Consulta la API de MyAnimeList para obtener animes populares
        url = "https://api.myanimelist.net/v2/anime/ranking"
        params = {
            "ranking_type": "bypopularity",  # Ordenar por popularidad
            "limit": 50,  # Máximo permitido por la API
            "offset": offset,  # Paginación
            "fields": "id,title,main_picture,start_date,end_date,synopsis,mean,rank,popularity,media_type,status,num_episodes,start_season,genres"
        }
        headers = {"X-MAL-CLIENT-ID": "389af6d7882d7b2315de49882a8d8451"}  # Reemplaza con tu Client ID
        response = requests.get(url, headers=headers, params=params)

        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail="Error al obtener datos de MyAnimeList")

        anime_list = response.json().get("data", [])
        if not anime_list:
            break  # Salir si no hay más animes disponibles

        for item in anime_list:
            anime_data = item["node"]

            # Verificar si el anime ya existe en la base de datos
            existing_anime = db.query(Anime).filter(Anime.id == anime_data["id"]).first()
            if not existing_anime:
                # Procesar y guardar el anime en la base de datos
                start_season = anime_data.get("start_season")
                start_season_str = f"{start_season['season']} {start_season['year']}" if start_season else None

                new_anime = Anime(
                    id=anime_data["id"],
                    title=anime_data["title"],
                    main_picture=anime_data["main_picture"]["medium"] if anime_data.get("main_picture") else None,
                    start_date=anime_data.get("start_date"),
                    end_date=anime_data.get("end_date"),
                    synopsis=anime_data.get("synopsis", ""),
                    mean=anime_data.get("mean"),
                    rank=anime_data.get("rank"),
                    popularity=anime_data.get("popularity"),
                    media_type=anime_data.get("media_type", ""),
                    status=anime_data.get("status", ""),
                    num_episodes=anime_data.get("num_episodes"),
                    start_season=start_season_str
                )

                db.add(new_anime)
                db.commit()
                db.refresh(new_anime)

                # Procesar y guardar los géneros asociados al anime
                for genre_data in anime_data.get("genres", []):
                    genre = db.query(Genre).filter(Genre.id == genre_data["id"]).first()
                    if not genre:
                        genre = Genre(id=genre_data["id"], name=genre_data["name"])
                        db.add(genre)
                        db.commit()  # Guardar inmediatamente para evitar problemas de FK
                        db.refresh(genre)

                    # Verificar si ya existe la relación anime-genre antes de insertarla
                    existing_anime_genre = (
                        db.query(AnimeGenre)
                        .filter_by(anime_id=new_anime.id, genre_id=genre.id)
                        .first()
                    )

                    if not existing_anime_genre:
                        anime_genre = AnimeGenre(anime_id=new_anime.id, genre_id=genre.id)
                        db.add(anime_genre)

                db.commit()  # Commit final para guardar todas las relaciones

                added_count += 1  # Incrementar el contador de animes añadidos
                if added_count >= 200:
                    break  # Salir si ya se añadieron 200 animes

        offset += 50  # Incrementar el offset para la siguiente página
        await asyncio.sleep(1)  # Espera de 1 segundo para no saturar la API

    return {"message": f"Se añadieron {added_count} animes populares a la base de datos."}