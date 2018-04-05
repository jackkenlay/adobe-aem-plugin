import com.intellij.openapi.ui.DialogWrapper;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

public class SettingsDialog  extends DialogWrapper {

    public SettingsDialog() {
        super(false);
        System.out.println("Settings Dialog Loaded");
        init();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JButton("Button"));

        return panel;
    }
}