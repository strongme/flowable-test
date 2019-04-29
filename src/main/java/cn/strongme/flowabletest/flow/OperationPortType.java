package cn.strongme.flowabletest.flow;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-29 09:48.
 */

public enum OperationPortType {

    Income("00"), Output("01"), OutputYesAndNo("02");

    private String value;

    OperationPortType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
