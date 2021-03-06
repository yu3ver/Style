package com.yalin.style.data.cache;

import android.text.TextUtils;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.data.entity.WallpaperEntity;

import java.util.Queue;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */
@Singleton
public class WallpaperCacheImpl implements WallpaperCache {

    private Queue<WallpaperEntity> wallpaperEntities;

    @Inject
    public WallpaperCacheImpl() {
    }

    @Override
    public Observable<WallpaperEntity> get() {
        Preconditions.checkNotNull(wallpaperEntities, "There is not cached wallpaper.");
        Preconditions.checkArgument(!wallpaperEntities.isEmpty(), "There is not cached wallpaper.");
        return Observable.create(emitter -> {
            emitter.onNext(wallpaperEntities.peek());
            emitter.onComplete();
        });
    }

    @Override
    public Observable<WallpaperEntity> getNext() {
        Preconditions.checkNotNull(wallpaperEntities, "There is not cached wallpaper.");
        Preconditions.checkArgument(!wallpaperEntities.isEmpty(), "There is not cached wallpaper.");
        return Observable.create(emitter -> {
            WallpaperEntity entity = wallpaperEntities.poll();
            wallpaperEntities.offer(entity);
            emitter.onNext(wallpaperEntities.peek());
            emitter.onComplete();
        });
    }

    @Override
    public Observable<Integer> getWallpaperCount() {
        Preconditions.checkNotNull(wallpaperEntities, "There is not cached wallpaper.");
        return Observable.create(emitter -> {
            emitter.onNext(wallpaperEntities.size());
            emitter.onComplete();
        });
    }

    @Override
    public void likeWallpaper(String wallpaperId) {
        Preconditions.checkNotNull(wallpaperId, "There is not cached wallpaper.");
        if (isCached(wallpaperId)) {
            WallpaperEntity entity = get(wallpaperId);
            if (entity != null) {
                entity.liked = !entity.liked;
            }
        }
    }

    @Override
    public synchronized void put(Queue<WallpaperEntity> wallpaperEntities) {
        this.wallpaperEntities = wallpaperEntities;
    }

    @Override
    public boolean isCached() {
        return wallpaperEntities != null && !wallpaperEntities.isEmpty();
    }

    @Override
    public boolean isCached(String wallpaperId) {
        if (isDirty()) {
            return false;
        }
        for (WallpaperEntity entity : wallpaperEntities) {
            if (TextUtils.equals(entity.wallpaperId, wallpaperId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDirty() {
        return wallpaperEntities == null;
    }

    @Override
    public synchronized void evictAll() {
        if (wallpaperEntities != null) {
            wallpaperEntities.clear();
        }
        wallpaperEntities = null;
    }

    private WallpaperEntity get(String wallpaperId) {
        for (WallpaperEntity entity : wallpaperEntities) {
            if (TextUtils.equals(entity.wallpaperId, wallpaperId)) {
                return entity;
            }
        }
        return null;
    }
}
