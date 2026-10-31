package framework.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseRenderer {

    private final String prefix;
    private final String suffix;

    public ResponseRenderer(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public void processJson(Object o, HttpServletResponse resp) throws IOException {
        String json = o instanceof String ? (String) o : new ObjectMapper().writeValueAsString(o);
        try (PrintWriter out = resp.getWriter()) {
            out.println(json);
        }
    }

    public void processModelAndView(Object result, HttpServletRequest req,
            HttpServletResponse resp, UrlMethod urlMethod) throws ServletException, IOException {
        ModelAndView mav = (ModelAndView) result;
        for (Map.Entry<String, Object> entry : mav.getValues().entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
        String view = mav.getView();
        if (view == null || view.isBlank()) {
            throw new ServletException("Aucune vue définie pour " + urlMethod);
        }
        RequestDispatcher dispatcher = req.getRequestDispatcher(prefix + view + suffix);
        dispatcher.forward(req, resp);
    }

    public void writeInvalidUrl(HttpServletResponse resp, UrlMethod urlMethod,
            Map<UrlMethod, java.lang.reflect.Method> urlControllers) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("URL invalide : " + urlMethod);
            out.println("URL valides :");
            for (UrlMethod key : urlControllers.keySet()) out.println(key);
        }
    }
}
