package controller.reader;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import controller.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import utils.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Map;
public class UpdateRPwdController {

    @FXML
    private AnchorPane updateRPwdMainPane;

    @FXML
    private JFXButton awRsure;

    @FXML
    private JFXButton awRcancel;

    @FXML
    private JFXPasswordField InputOldpwd;

    @FXML
    private JFXPasswordField InputNewpwd;

    @FXML
    private JFXPasswordField inputConfirmPwd;

    @FXML
    private Label labelMsg;

    private JdbcTemplate template = new JdbcTemplate(JDBCUtils.getDataSource());

    @FXML
    public void awRcancelClick() {
        InputNewpwd.setText("");
        InputOldpwd.setText("");
        inputConfirmPwd.setText("");
        labelMsg.setText("");
    }

    @FXML
    public void awRsureClick() {
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
        String readerID = Main.rc.getReaderId();
        System.out.println(readerID);
        //此处去查询一下读者的旧密码与填写的密码匹配，调用存储函数
        if(!queryForReadPassWord(readerID).equals(oldPwd)) {
            labelMsg.setText("旧密码输入错误，请重新输入！");
            return;
        }
        String sql = "UPDATE readers SET RPasswd = ? WHERE RId = ?";
        int cnt = template.update(sql, confirmPwd, readerID);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if(cnt > 0) {
            alert.setHeaderText("INFORMATION");
            alert.setContentText("修改密码成功！");
            alert.show();
            awRcancelClick();
        }
    }

    // 通过存储函数查询读者密码
    public String queryForReadPassWord(String reid) {
        Connection conn = null;
        CallableStatement call = null;
        String pwd = "";
        try {
            //1、获取数据库连接
            conn = JDBCUtils.getConnection();
            //2、定义调用存储函数的sql语句： {?= call <procedure-name>[(<arg1>,<arg2>, ...)]}
            String sql = "{?=call queryForReaderPwd(?)}";
            //3、获取执行sql对象
            call = conn.prepareCall(sql);
            //4、对于OUT参数（返回值），需要申明为输出参数类型
            call.registerOutParameter(1, OracleTypes.VARCHAR);
            //5、对于IN参数，需要赋值
            call.setString(2, reid);
            //6、执行调用
            call.execute();
            //7、取出读者密码
            pwd = call.getString(1);
            System.out.println("当前读者的密码为：" + pwd);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(null, call, conn);
        }
        return pwd;
    }
}
