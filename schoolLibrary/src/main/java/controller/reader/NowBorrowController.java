package controller.reader;

import emp.EmpBookLook;
import entity.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import utils.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class NowBorrowController {

    @FXML
    private TableView<EmpBookLook> nBBookView;

    @FXML
    private TableColumn<EmpBookLook, String> nBBookId;

    @FXML
    private TableColumn<EmpBookLook, String> nBBookName;

    @FXML
    private TableColumn<EmpBookLook, String> nBBookAuthor;

    @FXML
    private TableColumn<EmpBookLook, String> nBBookStart;

    @FXML
    private TableColumn<EmpBookLook, String> nBBookEnd;

    private JdbcTemplate template = new JdbcTemplate(JDBCUtils.getDataSource());

    //初始化当前借阅页面的数据
    public void initData(String readerId) {
        // 此处使用程序包
        Connection conn = null;
        CallableStatement call = null;
        ResultSet rs = null;
        ObservableList<EmpBookLook> data = null;
        try {
            //1、获取数据库连接
            conn = JDBCUtils.getConnection();
            // 报错：java.lang.ClassCastException: com.alibaba.druid.pool.DruidPooledConnection cannot be cast to oracle.jdbc.OracleConnection
            // 失败原因：The connection pool usually has a wrapper around the real connection instance, that's why your cast fails.
            // 即连接池通常包装了一个真实的Connection实例。
            // 增加此代码，将Connection转换为OracleConnection
            // 解决方案：connection = connection.unwrap(OracleConnection.class);
            conn = conn.unwrap(OracleConnection.class);
            //2、定义调用存储过程的sql语句： {call <procedure-name>[(<arg1>,<arg2>, ...)]}
            String sql = "{call MYPACKAGE.queryEmpList(?,?)}";
            //3、获取执行sql对象
            call = conn.prepareCall(sql);
            //4、对于IN参数，需要赋值
            call.setString(1, readerId);
            //5、对于OUT参数，需要申明输出参数的类型
            call.registerOutParameter(2, OracleTypes.CURSOR);
            //6、执行调用
            call.execute();
            //7、从结果集循环取出每条记录
            rs = ((OracleCallableStatement) call).getCursor(2);
            data = FXCollections.observableArrayList();
            while(rs.next()) {
                Book book = new Book();
                book.setBId(rs.getString("BId"));
                book.setBName(rs.getString("BName"));
                book.setBAuthor(rs.getString("BAuthor"));
                // date 转 Localdate
                book.setNBTime(rs.getDate("NBTime").toLocalDate());
                book.setNBRTime(rs.getDate("NBRTime").toLocalDate());
                System.out.println(book);
                data.add(new EmpBookLook(book));
            }
            nBBookView.setItems(data);
            nBBookId.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("BId"));
            nBBookName.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("BName"));
            nBBookAuthor.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("BAuthor"));
            nBBookStart.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("NBTime"));
            nBBookEnd.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("NBRTime"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(rs, call, conn);
        }
        /*String sql = "SELECT * FROM nborrowing, readers, book WHERE book.BId = nborrowing.NBId AND readers.RId = nborrowing.NRId AND nborrowing.NState = 1 AND readers.RId = ?";
        List<Book> nbList = template.query(sql, new BeanPropertyRowMapper<>(Book.class), readerId);
        ObservableList<EmpBookLook> data = FXCollections.observableArrayList();
        System.out.println("查询结果的数量有" + nbList.size());
        for(int i = 0; i < nbList.size(); ++i) {
            System.out.println(nbList.get(i));
            data.add(new EmpBookLook(nbList.get(i)));
        }*/
        /*nBBookView.setItems(data);
        nBBookId.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("BId"));
        nBBookName.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("BName"));
        nBBookAuthor.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("BAuthor"));
        nBBookStart.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("NBTime"));
        nBBookEnd.setCellValueFactory(new PropertyValueFactory<EmpBookLook, String>("NBRTime"));*/
    }
}