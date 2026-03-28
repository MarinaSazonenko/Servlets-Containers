package servlet;

import com.google.gson.Gson;
import constants.HttpMethod;
import model.Post;
import repository.PostRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class PostServlet extends HttpServlet {
    private final PostRepository repository;
    private final Gson gson;

    public PostServlet(PostRepository repository) {
        this.repository = repository;
        this.gson = new Gson();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Обработка CORS (если нужно)
        resp.setContentType("application/json;charset=UTF-8");

        String method = req.getMethod();
        String pathInfo = req.getPathInfo(); // например, /api/posts или /api/posts/1

        try {
            if (HttpMethod.GET.equals(method) && (pathInfo == null || "/".equals(pathInfo))) {
                getAllPosts(resp);
            } else if (HttpMethod.GET.equals(method) && pathInfo != null && !"/".equals(pathInfo)) {
                getPostById(pathInfo, resp);
            } else if (HttpMethod.POST.equals(method)) {
                createOrUpdatePost(req, resp);
            } else if (HttpMethod.DELETE.equals(method) && pathInfo != null) {
                deletePost(pathInfo, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                resp.getWriter().write("{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void getAllPosts(HttpServletResponse resp) throws IOException {
        Map<Long, Post> posts = repository.findAll();
        resp.getWriter().write(gson.toJson(posts.values()));
    }

    private void getPostById(String pathInfo, HttpServletResponse resp) throws IOException {
        long id = extractIdFromPath(pathInfo);
        Post post = repository.findById(id);
        if (post != null) {
            resp.getWriter().write(gson.toJson(post));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Post not found\"}");
        }
    }

    private void createOrUpdatePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Post post = gson.fromJson(req.getReader(), Post.class);
        Post saved = repository.save(post);
        if (saved != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(saved));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Post not found for update\"}");
        }
    }

    private void deletePost(String pathInfo, HttpServletResponse resp) {
        long id = extractIdFromPath(pathInfo);
        repository.deleteById(id);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private long extractIdFromPath(String pathInfo) {
        String[] parts = pathInfo.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid path");
        }
        return Long.parseLong(parts[1]);
    }
}
