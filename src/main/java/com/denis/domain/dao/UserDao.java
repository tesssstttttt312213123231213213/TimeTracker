package com.denis.domain.dao;

import com.denis.domain.User;
import com.denis.domain.exceptions.DAOException;
import com.denis.domain.factories.ConfigFactory;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class UserDao {
    private static UserDao instance;

    private static Configuration exceptionsConfig;
    private static Configuration statementsConfig;

    private Connection connection;

    private static Logger logger;

    private UserDao() {
        logger = LogManager.getLogger();
        exceptionsConfig = ConfigFactory.getConfigByName("exceptions");
        statementsConfig = ConfigFactory.getConfigByName("statements");
    }

    public static UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }
        return instance;
    }

    public void createUser(User user) throws DAOException {
        if (user == null) {
            exceptionsConfig.setProperty("failedParameter", "User");
            throw new DAOException(new NullPointerException(
                    exceptionsConfig.getString("parameterNull")
            ));
        }
        createUser(user.getUsername(), user.getPassword(), user.getName());
    }

    public void createUser(String username, String password, String name) throws DAOException {
        if (username == null || password == null || name == null) {
            if      (username == null) exceptionsConfig.setProperty("failedParameter", "Username"); // TODO: 5/31/22 make it in normal
            else if (password == null) exceptionsConfig.setProperty("failedParameter", "Password");
            else                       exceptionsConfig.setProperty("failedParameter", "Name");
            throw new DAOException(new NullPointerException(
                    exceptionsConfig.getString("parameterNull")
            ));
        }

        String createUser;
        PreparedStatement statement = null;

        try {
            createUser = statementsConfig.getString("createUser");
            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(createUser);

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, name);

            statement.execute();
        } catch (SQLException e) {
            exceptionsConfig.setProperty("failedUser", new UserDto(0, username, password, name)); // 0 because we can retrieve id only from db, here exception is throwing => record not created => user doesn't have id
            throw new DAOException(exceptionsConfig.getString("createUserFail"), e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                logger.error(exceptionsConfig.getString("closeStatementFail"), new DAOException(e));
                // TODO: 5/31/22 ask about: я ловлю SQLException, но у меня может вылетить и НПЕ. Указывать в catch(Exception e)? Что бы ловить все exceptions? Но это плохая практика.
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error(exceptionsConfig.getString("closeConnectionFail"), new DAOException(e));
            }
        }
    }

    public int retrieveId(String username) throws DAOException {
        if (username == null) {
            exceptionsConfig.setProperty("failedParameter", "Username");
            throw new DAOException(new NullPointerException(
                    exceptionsConfig.getString("parameterNull")
            ));
        }

        ResultSet resultSet = null;
        PreparedStatement statement = null;
        String getIdQuery;

        try {
            getIdQuery = statementsConfig.getString("getUserId");

            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(getIdQuery);
            statement.setString(1, username);
            resultSet = statement.executeQuery();

            resultSet.next();
            return resultSet.getInt("UserID");
        } catch (SQLException e) {
            exceptionsConfig.setProperty("failedUsername", username);
            throw new DAOException(exceptionsConfig.getString("retrieveUserIdFail"), e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                logger.error(exceptionsConfig.getString("closeResultSetFail"), new DAOException(e));
            }

            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                logger.error(exceptionsConfig.getString("closeStatementFail"), new DAOException(e));
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error(exceptionsConfig.getString("closeConnectionFail"), new DAOException(e));
            }
        }
    }

    public UserDto retrieveUserDto(String username, String password) throws DAOException {
        if (username == null || password == null) {
            if (username == null) exceptionsConfig.setProperty("failedParameter", "Username"); // TODO: 5/31/22 make it in normal
            else                  exceptionsConfig.setProperty("failedParameter", "Password");
            throw new DAOException(new NullPointerException(
                    exceptionsConfig.getString("parameterNull")
            ));
        }

        ResultSet resultSet = null;
        PreparedStatement statement = null;
        String getIdQuery;

        try {
            getIdQuery = statementsConfig.getString("getUser");

            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(getIdQuery);
            statement.setString(1, username);
            statement.setString(2, password);
            resultSet = statement.executeQuery();

            resultSet.next();
            UserDto userDto = new UserDto(
                    resultSet.getInt("UserID"),
                    resultSet.getString("UserName"),
                    resultSet.getString("Password"),
                    resultSet.getString("Name")
            );
            logger.debug("Was returned " + userDto);
            return userDto;
        } catch (SQLException e) {
            exceptionsConfig.setProperty("failedUsername", username);
            throw new DAOException(exceptionsConfig.getString("retrieveUserIdFail"), e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                logger.error(exceptionsConfig.getString("closeResultSetFail"), new DAOException(e));
            }

            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                logger.error(exceptionsConfig.getString("closeStatementFail"), new DAOException(e));
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error(exceptionsConfig.getString("closeConnectionFail"), new DAOException(e));
            }
        }
    }
}
