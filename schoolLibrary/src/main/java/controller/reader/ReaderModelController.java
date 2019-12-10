package controller.reader;

import controller.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import utils.JDBCUtils;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class ReaderModelController {

    private JdbcTemplate template = new JdbcTemplate(JDBCUtils.getDataSource());

    @FXML
    private VBox vbox;

    @FXML
    public void loadBreakMsg() throws Exception {
        System.out.println("加载违章记录界面！");
        try {
            FXMLLoader voLoader = new FXMLLoader(getClass().getResource("/fxml/reader/violation.fxml"));
            Parent bhparent = voLoader.load();
            ViolationController vc = (ViolationController) voLoader.getController();
            Stage stage = new Stage();
            stage.setTitle("违章记录");
            stage.setScene(new Scene(bhparent, Main.sonWidth, Main.sonHeight));
            stage.setResizable(false);
            Stage stageF = (Stage) vbox.getScene().getWindow();
            //通过数据库查询来设置借阅和归还信息，设置模态
            stage.initOwner(stageF);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
            vc.initUncomplyData(Main.rc.getReaderId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadChangPwd() throws Exception {
        switchToPage("/fxml/reader/updateRPwd.fxml", "修改密码");
        //不用写
        System.out.println("加载修改密码界面！");
    }

    @FXML
    public void loadHistoryBorrowMsg() throws Exception {
        try {
            FXMLLoader bhLoader = new FXMLLoader(getClass().getResource("/fxml/reader/borrowHistory.fxml"));
            Parent bhparent = bhLoader.load();
            BorrowHistoryController nbc = (BorrowHistoryController) bhLoader.getController();
            Stage stage = new Stage();
            stage.setTitle("借阅历史");
            stage.setScene(new Scene(bhparent, Main.sonWidth, Main.sonHeight));
            stage.setResizable(false);
            //填充数据
            nbc.initHistoryData(Main.rc.getReaderId());
            Stage stageF = (Stage) vbox.getScene().getWindow();
            //通过数据库查询来设置借阅和归还信息，设置模态
            stage.initOwner(stageF);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("加载历史借阅界面！");
    }

    @FXML
    public void loadNowBorrowMsg() throws Exception {
        try {
            FXMLLoader nbLoader = new FXMLLoader(getClass().getResource("/fxml/reader/nowBorrow.fxml"));
            Parent nbparent = nbLoader.load();
            NowBorrowController nbc = (NowBorrowController) nbLoader.getController();
            Stage stage = new Stage();
            stage.setTitle("正在借阅表");
            stage.setScene(new Scene(nbparent, Main.sonWidth, Main.sonHeight));
            stage.setResizable(false);
            //填充数据
            nbc.initData(Main.rc.getReaderId());
            Stage stageF = (Stage) vbox.getScene().getWindow();
            //通过数据库查询来设置借阅和归还信息，设置模态
            stage.initOwner(stageF);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("加载正在借阅界面！");
    }

    @FXML
    public void loadOrderMsg() throws Exception {
        try {
            FXMLLoader rbbLoader = new FXMLLoader(getClass().getResource("/fxml/reader/reserveBook.fxml"));
            Parent rbbparent = rbbLoader.load();
            ReserveBookController rbc = (ReserveBookController) rbbLoader.getController();
            Stage stage = new Stage();
            stage.setTitle("预约信息");
            stage.setScene(new Scene(rbbparent, Main.sonWidth, Main.sonHeight));
            stage.setResizable(false);
            //填充数据
            rbc.initReserveData(Main.rc.getReaderId());
            Stage stageF = (Stage) vbox.getScene().getWindow();
            //通过数据库查询来设置借阅和归还信息，设置模态
            stage.initOwner(stageF);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("加载预约信息界面！");
    }

    @FXML
    public void loadPersonMsg() {
        Connection conn = null;
        CallableStatement call = null;
        try {
            FXMLLoader rLoader = new FXMLLoader(getClass().getResource("/fxml/reader/readerPmsg.fxml"));
            Parent rparent = rLoader.load();
            ReaderPmsgController rpc = (ReaderPmsgController) rLoader.getController();
            Stage stage = new Stage();
            stage.setTitle("个人信息");
            stage.setScene(new Scene(rparent, Main.width, Main.height));
            stage.setResizable(false);
            // 查询数据库
            String rId = Main.rc.getReaderId();
            System.out.println("当前读者id为：" + rId);
            // 此处用存储过程
            //1、获取数据库连接
            conn = JDBCUtils.getConnection();
            //2、定义调用存储过程的sql语句： {call <procedure-name>[(<arg1>,<arg2>, ...)]}
            String sql = "{call queryForReaderMessage(?,?,?,?,?,?,?,?)}";
            //3、获取执行sql对象
            call = conn.prepareCall(sql);
            //4、对于IN参数，需要赋值
            call.setString(1, rId);
            //5、对于OUT参数，需要申明为输出参数类型
            call.registerOutParameter(2, OracleTypes.VARCHAR);
            call.registerOutParameter(3, OracleTypes.VARCHAR);
            call.registerOutParameter(4, OracleTypes.VARCHAR);
            call.registerOutParameter(5, OracleTypes.VARCHAR);
            call.registerOutParameter(6, OracleTypes.INTEGER);
            call.registerOutParameter(7, OracleTypes.INTEGER);
            call.registerOutParameter(8, OracleTypes.INTEGER);
            //6、执行调用
            call.execute();
            //7、取出所有结果
            String rName = call.getString(2);
            String rSex = call.getString(3);
            String job = call.getString(4);
            String rDate = call.getString(5);
            Integer rNum1 = call.getInt(6);
            Integer rTotal1 = call.getInt(7);
            Integer rSates = call.getInt(8);

            rpc.initData(rId, rName, rSex, job, rDate, rNum1, rTotal1,rSates);
            Stage stageF = (Stage) vbox.getScene().getWindow();
            //通过数据库查询来设置借阅和归还信息，设置模态
            stage.initOwner(stageF);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        System.out.println("加载个人信息界面！");
    }


    private void switchToPage(String loc, String title) throws Exception{
        FXMLLoader Loader = new FXMLLoader(getClass().getResource(loc));
        Parent parent = Loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(parent, Main.sonWidth, Main.sonHeight));
        stage.setResizable(false);
        Stage stageF = (Stage) vbox.getScene().getWindow();
        //通过数据库查询来设置借阅和归还信息，设置模态
        stage.initOwner(stageF);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}
