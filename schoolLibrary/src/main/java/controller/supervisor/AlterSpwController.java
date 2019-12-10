package controller.supervisor;

import controller.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import utils.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Map;
import java.util.Optional;

public class AlterSpwController {

    @FXML
    private Button awSsure;

    @FXML
    private Button awScancel;

    @FXML
    private PasswordField InputNewpwd;

    @FXML
    private PasswordField inputConfirmPwd;

    @FXML
    private PasswordField InputOldpwd;

    @FXML
    private Label labelMsg;

    @FXML
    private AnchorPane superPane;

    private JdbcTemplate template = new JdbcTemplate(JDBCUtils.getDataSource());

    @FXML
    public void awScancelClick() {
        InputNewpwd.setText("");
        InputOldpwd.setText("");
        inputConfirmPwd.setText("");
        labelMsg.setText("");
    }

    @FXML
    public void awSsureClick() {
        String oldPwd = InputOldpwd.getText();
        String newPwd = InputNewpwd.getText();
        String confirmPwd =  inputConfirmPwd.getText();
        System.out.println("旧密码：" + oldPwd);
        System.out.println("新密码：" + newPwd);
        System.out.println("确认密码：" + confirmPwd);
        boolean flag = false;
        if(oldPwd == null || oldPwd.equals("")) {
            flag = true;
        }
        if(newPwd == null || newPwd.equals("")) {
            flag = true;
        }
        if(confirmPwd == null || confirmPwd.equals("")) {
            flag = true;
        }
        if(flag) {
            labelMsg.setText("输入不能为空~");
            return;
        }
        if(!newPwd.equals(confirmPwd)) {
            labelMsg.setText("新密码与确认密码不一致，请重新输入！");
            return;
        }
        //此处去查询读者账号是否存在，不存在则直接设置错误信息
        if(!queryForSuperPwd("root").equals(oldPwd)) {
            labelMsg.setText("旧密码输入错误，请重新输入！");
            return;
        }
        String sql = "UPDATE supermanager SET SPasswd = ? WHERE Sid = 'root'";
        int cnt = template.update(sql, confirmPwd);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Optional<ButtonType> type = null;
        if(cnt > 0) {
            alert.setHeaderText("INFORMATION");
            alert.setContentText("修改密码成功！");
            type = alert.showAndWait();
            if(type.isPresent() && type.get() == ButtonType.OK) {
                //清空表单数据
                awScancelClick();
                //登录信息失效，请重新登录
                alert.setAlertType(Alert.AlertType.WARNING);
                alert.setHeaderText("WARNING!");
                alert.setContentText("密码已修改，为确保安全，请重新登录！");
                type = alert.showAndWait();
                if(type.isPresent() && (type.get() == ButtonType.OK || type.get() == ButtonType.CLOSE)) {
                    turnToLogin();
                }
            } else {
                awScancelClick();
                turnToLogin();
            }
        }
    }

    // 查询超管密码
    public String queryForSuperPwd(String superId) {
        Connection conn = null;
        CallableStatement call = null;
        String sPwd = "";
        try {
            //1、获取数据库连接
            conn = JDBCUtils.getConnection();
            //2、定义调用存储函数的sql语句： {?= call <procedure-name>[(<arg1>,<arg2>, ...)]}
            String sql = "{?=call queryForSuperManagerPwd(?)}";
            //3、获取执行sql对象
            call = conn.prepareCall(sql);
            //4、对于OUT参数（返回值），需要申明为输出参数类型
            call.registerOutParameter(1, OracleTypes.VARCHAR);
            //5、对于IN参数，需要赋值
            call.setString(2, superId);
            //6、执行调用
            call.execute();
            //7、取出超管密码
            sPwd = call.getString(1);
            System.out.println("该超管密码为：" + sPwd);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(null, call, conn);
        }
        return sPwd;
    }

    private void turnToLogin() {
        System.out.println("跳转到登录界面！");
        //获取外层的面板
        Stage stage  = (Stage) Main.sac.getBorderPane().getScene().getWindow();
        stage.setScene(Main.lcScene);
        //设置登录信息
        Main.lc.clearContent();
    }
}
