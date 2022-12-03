package my.pak;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.pak.UtilModel;

@WebServlet(value={"/TigerServ"})
public class TigerServ
extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long l = System.currentTimeMillis();
        UtilModel um = new UtilModel();
        response.getWriter().append("Served jar:").append(Long.toString(l)).append(":").append(request.getContextPath()).append(":").append(um.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}

