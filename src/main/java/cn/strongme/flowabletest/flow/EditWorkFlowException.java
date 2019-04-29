package cn.strongme.flowabletest.flow;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-26 09:36.
 */


public class EditWorkFlowException extends RuntimeException {

    private static final long serialVersionUID = 4275250474527650071L;

    public EditWorkFlowException(Throwable cause) {
        super(cause);
    }

    public EditWorkFlowException(String message) {
        super(message);
    }

    public EditWorkFlowException(String message, Throwable cause) {
        super(message, cause);
    }

}
