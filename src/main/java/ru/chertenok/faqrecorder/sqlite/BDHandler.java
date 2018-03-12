package ru.chertenok.faqrecorder.sqlite;


import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BDHandler {
    private static Logger log = Logger.getGlobal();
    private static String connectionStr;
    private static Connection connection;


    private BDHandler() {
    }

    public static void init(String connectionStr, String nameBD) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        BDHandler.connectionStr = connectionStr;

        connection = DriverManager.getConnection(String.format(connectionStr, nameBD));

        log.log(Level.INFO, String.format(connectionStr, nameBD));
    }

    public static void close() throws SQLException {
        connection.close();

    }

    /*  public static String getFileID(String fullFileName, String userLogin) throws Exception {
          String result = getHashCode(fullFileName + userLogin, Utils.HashCode.SH256);
          if (!isHashNotExistInBd(result)) {
              result = getHashCode(fullFileName + userLogin + System.currentTimeMillis(), Utils.HashCode.SH256);
              if (isHashNotExistInBd(result)) {
                  log.log(Level.SEVERE, "HashID is dublicate: hash - " + result + " , file  - " + fullFileName + ", user " + userLogin);
                  throw new Exception("HashID is dublicate");
              }
          }
          return result;
      }

      public static boolean isHashNotExistInBd(String hash) throws SQLException {
          return getFirstIntFromSelect("select count(*) from repository where Hash = ?", hash) == 0;
      }

      public static void registerUser(String userName, String userPassword) throws SQLException {
          executeQueryWithNoResult("insert INTO users (login,password) VALUES (?,?)",
                  userName.trim(), getHashCode(userPassword.trim(), Utils.HashCode.SH256));
          log.log(Level.INFO, "registered user [" + userName + "] inserted in bd");
      }

      public static boolean isUserNameAndPasswordTrue(String userName, String userPassword) throws Exception {
          return getFirstIntFromSelect("select count(*) from users where login = ? and password = ?",
                  userName.trim(), getHashCode(userPassword.trim(), Utils.HashCode.SH256)) != 0;
      }

      public static void addUserFileToBD(String userName, FileInfo fi) throws SQLException {
          executeQueryWithNoResult("insert INTO repository (Hash,UserLogin,UserFileName,userFullName,Size,dt) VALUES (?,?,?,?,?,?)",
                  fi.ID, userName, fi.fileName, fi.SourceFileName, String.valueOf(fi.fileSize), fi.fileDT);
          log.log(Level.INFO, "file [" + fi.fileName + "] inserted in bd");
      }


      public static boolean isUserNameExistInBD(String userLogin) throws SQLException {
          return getFirstIntFromSelect("select count(*) from users where login = ?", userLogin.trim()) == 0;
      }

      public static boolean isFileExistInBD(String file, String userLogin) throws SQLException {
          return getFirstIntFromSelect("select count(*) from repository where UserLogin = ? and UserFileName = ?", userLogin, file) != 0;
      }

      public static FileInfo[] getFileList(String userLogin) throws SQLException, IOException {
          log.log(Level.INFO, "select files from bd to user [" + userLogin + "] ");
          PreparedStatement st = connection.prepareStatement("select Hash,UserFileName,userFullName,dt, Size from repository where UserLogin = ?");
          st.setString(1, userLogin);
          ResultSet rs = st.executeQuery();
          ArrayList<FileInfo> list = new ArrayList<>();

          if (rs != null) {
              while (rs.next()) {
                  FileInfo fi = new FileInfo(rs.getString("UserFileName"),
                          rs.getString("userFullName"), rs.getString("dt"),
                          rs.getLong("Size"), rs.getString("Hash"));
                  list.add(fi);
              }
              rs.close();
          }
          st.close();
          FileInfo[] res = new FileInfo[list.size()];
          return list.toArray(res);
      }


      public static void deleteFileFromBD(String id, String userLogin) throws SQLException {
          executeQueryWithNoResult("delete from repository where Hash = ? and UserLogin = ?", id, userLogin);
      }

  */
    public static String getFirstStringFromSelect(String sql, String... params) throws SQLException, IllegalArgumentException {
        PreparedStatement st = connection.prepareStatement(sql);
        if (st.getParameterMetaData().getParameterCount() != params.length)
            throw new IllegalArgumentException("params count != sql params count ");
        for (int i = 0; i < st.getParameterMetaData().getParameterCount(); i++) {
            st.setString(i + 1, params[i]);
        }
        ResultSet rs = st.executeQuery();
        String res = rs.getString(1);
        rs.close();
        st.close();
        return res;
    }

    public static ResultSet getResultSetFromSelect(String sql, String... params) throws SQLException, IllegalArgumentException {
        PreparedStatement st = connection.prepareStatement(sql);
        if (st.getParameterMetaData().getParameterCount() != params.length)
            throw new IllegalArgumentException("params count != sql params count ");
        for (int i = 0; i < st.getParameterMetaData().getParameterCount(); i++) {
            st.setString(i + 1, params[i]);
        }
        ResultSet rs = st.executeQuery();

        //st.close();
        return rs;
    }


    public static int getFirstIntFromSelect(String sql, String... params) throws SQLException, IllegalArgumentException {
        return Integer.valueOf(getFirstStringFromSelect(sql, params));
    }


    public static void executeQueryWithNoResult(String sql, String... params) throws SQLException {
        PreparedStatement st = connection.prepareStatement(sql);
        if (st.getParameterMetaData().getParameterCount() != params.length)
            throw new IllegalArgumentException("params count != sql params count ");
        for (int i = 0; i < st.getParameterMetaData().getParameterCount(); i++) {
            st.setString(i + 1, params[i]);
        }
        st.execute();
        st.close();
    }

}
