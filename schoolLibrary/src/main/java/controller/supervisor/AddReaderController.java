package controller.supervisor;

import controller.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import utils.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

public class AddReaderController {

    @FXML
    private TextField InputRname;

    @FXML
    private ComboBox<String> selectRsex;

    @FXML
    private TextField InputRaccount;

    @FXML
    private ComboBox<String> selectRunit;

    @FXML
    private TextField InputRpw;

    @FXML
    private Button Rsure;

    @FXML
    private Button Rcancel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label lableName;

    @FXML
    private Label labelSex;

    @FXML
    private Label labelAccount;

    @FXML
    private Label labelId;

    @FXML
    private Label labelPwd;

    @FXML
    private Label labelDate;

    @FXML
    private Label dataMsg;

    private JdbcTemplate template = new JdbcTemplate(JDBCUtils.getDataSource());

    // 重置按钮
    @FXML
    public void RcancelClick() {
        InputRname.setText("");
        InputRaccount.setText("");
        InputRpw.setText("");
        //默认选择第一个子项
        selectRsex.getSelectionModel().select(0);
        // 默认选择第一个子项
        selectRunit.getSelectionModel().select(0);
        // 传入为空
        datePicker.setValue(null);
        lableName.setText("");
        labelSex.setText("");
        labelId.setText("");
        labelDate.setText("");
        labelPwd.setText("");
        labelAccount.setText("");
        dataMsg.setText("");
    }

    @FXML
    public void RsureClick() throws ParseException {
        boolean flag = false;
        String rName = InputRname.getText();
        if(rName == null || rName.equals("")) {
            lableName.setText("姓名不能为空~");
            flag = true;
        } else {
            lableName.setText("");
        }
        String rAccount = InputRaccount.getText();
        if(rAccount == null || rAccount.equals("")) {
            labelAccount.setText("账号不能为空~");
            flag = true;
        } else {
            String strRegex = "^[\\d]{11}$";
            boolean isNumId = rAccount.matches(strRegex);
            System.out.println("+++++" + isNumId);
            if (!isNumId) {
                labelAccount.setText("输入有误，提示：11位有效数字~");
                flag = true;
            } else {
                labelAccount.setText("");
            }
        }
        String rPwd = InputRpw.getText();
        if(rPwd == null || rPwd.equals("")) {
            labelPwd.setText("密码不能为空~");
            flag = true;
        } else {
            labelPwd.setText("");
        }

        //弹窗或在窗口页面中显示错误
        String sex = selectRsex.getValue();
        if(sex == null || sex.equals("选择性别")) {
            labelSex.setText("请选择性别~");
            flag = true;
        } else {
            labelSex.setText("");
        }
        String idCard = selectRunit.getValue();
        if(idCard == null || idCard.equals("选择身份")) {
            labelId.setText("请选择身份~");
            flag = true;
        } else {
            labelId.setText("");
        }
        //获取日期
        LocalDate date = datePicker.getValue();
        if(date == null) {
            labelDate.setText("日期输入有误");
            dataMsg.setText("提示：年-月-日");
            flag = true;
        } else {
            if(!flag) datePicker.setValue(null);
            labelDate.setText("");
            dataMsg.setText("");
        }
        System.out.println(rName + " " + rAccount + " " + rPwd + " " + sex + " " + idCard + " " + date);

        if(flag) return;
        String sql = "SELECT COUNT(*) FROM readers where Rid = ?";
        Long cnt = template.queryForObject(sql, Long.class, rAccount);
        if(cnt > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("添加读者状态");
            alert.setContentText("添加失败，插入2个相同的读者账号~");
            alert.showAndWait();
        } else {
            // 添加读者
            int count = insertIntoReader(rAccount, rPwd, rName, sex, idCard, date.toString());
            if(count > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("添加读者状态");
                alert.setContentText("成功添加一名读者：" + rName + "。");
                alert.show();
                //清空表单数据
                RcancelClick();
                System.out.println("插入记录成功！");
            } else {
                System.out.println("插入记录失败~");
            }
        }
    }
    // 添加读者
    public int insertIntoReader(String v_RId, String v_RPasswd, String v_RName, String v_RSex, String v_RUnit, String v_RDate) {
        Connection conn = null;
        CallableStatement call = null;
        int res = 0;
        try {
            //1、获取数据库连接
            conn = JDBCUtils.getConnection();
            //2、定义调用存储过程的sql语句： {call <procedure-name>[(<arg1>,<arg2>, ...)]}
            String sql = "{call insertAReader(?,?,?,?,?,?,?)}";
            //3、获取执行sql对象
            call = conn.prepareCall(sql);
            //4、对于IN参数，需要赋值
            call.setString(1, v_RId);
            call.setString(                           2, v_RPasswd);
            call.setString(3, v_RName);
            call.setString(4, v_RSex);
            call.setString(5, v_RUnit);
            call.setString(6, v_RDate);
            //5、对于OUT参数，需要申明为输出参数类型
            call.registerOutParameter(7, OracleTypes.NUMBER);
            //6、执行调用
            call.execute();
            //7、取出结果
            res = call.getInt(7);
            System.out.println("是否插入成功：" + res);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(null, call, conn);
        }
        return res;
    }

    @FXML
    public void onclickSelectRsex() {
        //获取子项的下标值
        System.out.println(selectRsex.getSelectionModel().getSelectedIndex());
    }

    @FXML
    public void onClickSelectRUnit() {
        // 获得下标
        System.out.println(selectRunit.getSelectionModel().getSelectedIndex());
        System.out.println(selectRunit.getItems().get(0));
    }
}
