package repository;

import model.Post;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PostRepository {
    private final Map<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    // Сохранить (создать или обновить)
    public Post save(Post post) {
        if (post.getId() == 0) {
            // Новый пост
            long newId = idCounter.getAndIncrement();
            Post newPost = new Post(newId, post.getContent());
            posts.put(newId, newPost);
            return newPost;
        } else {
            // Обновление существующего
            long id = post.getId();
            if (posts.containsKey(id)) {
                Post existing = posts.get(id);
                existing.setContent(post.getContent());
                return existing;
            } else {
                // Стратегия: возвращаем null или кидаем исключение. Здесь вернём null.
                // Можно также создать новый пост, но по ТЗ стратегию определяем сами.
                return null;
            }
        }
    }

    // Найти по id
    public Post findById(long id) {
        return posts.get(id);
    }

    // Получить все посты
    public Map<Long, Post> findAll() {
        return posts;
    }

    // Удалить по id
    public void deleteById(long id) {
        posts.remove(id);
    }
}
