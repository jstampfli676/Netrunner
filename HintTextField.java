import javax.swing.*;
import java.awt.event.*;

public class HintTextField extends JTextField implements FocusListener {
	private final String hint;
	private boolean showingHint;

	public HintTextField(final String hint) {
		super(hint);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if(this.getText().isEmpty()) {
		  super.setText("");
		  showingHint = false;
		}
	}
	@Override
	public void focusLost(FocusEvent e) {
		if(this.getText().isEmpty()) {
		  super.setText(hint);
		  showingHint = true;
		}
	}

	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}
        
        @Override
        public void setText(String t) {
            showingHint = false;
            super.setText(t);
        }
}