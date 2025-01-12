package com.denis.view;

import com.denis.control.Protector;
import com.denis.domain.User;
import com.denis.domain.exceptions.ControlException;
import com.denis.domain.factories.ConfigFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;

public class Registration extends HttpServlet {
    private Logger logger;
    private Protector protector;
    private Configuration exceptionConfig;

    public Registration() {
        logger = LogManager.getLogger();
        protector = Protector.getInstance();
        exceptionConfig = ConfigFactory.getConfigByName("exceptions");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("get in registration");
        resp.setContentType("text/html");
        req.getRequestDispatcher("/links.html").include(req, resp);
        try {
            protector.checkUserAuthorization(req);
            logger.debug("redirecting to workspace");
            resp.sendRedirect("/timeTracker/workspace");
        } catch (ControlException e) {
            logger.info("successfully include registration.html"); // TODO: 6/15/22 find by logger.info or logger.debug
            logger.error(e.getMessage(), e);
            req.getRequestDispatcher("/registration.html").include(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        try {
            logger.debug("Start registering process");
            protector.registerUser(req, resp);
            logger.debug("Registering is success");
            resp.sendRedirect("/timeTracker/workspace");
        } catch (ControlException e) {
            PrintWriter out = resp.getWriter();

            logger.error(e.getMessage(), e);

            req.getRequestDispatcher("/links.html").include(req, resp);
            req.getRequestDispatcher("/registration.html").include(req, resp); // TODO: 6/6/22 repeated logic (such as that in doGet() method)
            out.println(
                    "<h3 style=\"color: red; text-align: center;\">" + exceptionConfig.getString("usernameAlreadyTaken") +"</h3>"
            );
        }
    }
}
