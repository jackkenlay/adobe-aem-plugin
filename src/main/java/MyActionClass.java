import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.*;

public class MyActionClass extends AnAction {

    private String currentDir = "";
    private String componentName = "";
    private String componentGroup = "";
    private String componentCategory = "";
    private boolean createClientLibs;
    private boolean createFullClientLibs;

    @Override
    public void actionPerformed(AnActionEvent e) {

        /*
         * TO DO
         * Set focus of input text field
         * right click - create client libs
         * right click - create client libs -> all
         * right click - create client libs -> js
         * right click - create client libs -> css
         * Configurable settings
         *  - default component group
         *  - if null, then can save new entry
         *  - default html/less/js templates (hard)
         * Ensure dialog is smooth experience with keyboard
         * Test export
         * Round off ReadME
         * have checker for less or SASS
         * auto find components folder for keyboard shortcut
         */

        //componentName = JOptionPane.showInputDialog(null,"Enter component name:","my-component");

        JTextField componentNameInput = new JTextField();
        JTextField componentGroupInput = new JTextField();
        JTextField componentCategoryInput = new JTextField();

        JCheckBox createClientLibsChkBx = new JCheckBox();
        createClientLibsChkBx.setSelected(true);

        JCheckBox createFullClientLibsChkBx = new JCheckBox();
        createFullClientLibsChkBx.setSelected(true);

        Object[] message = {
                "Component Name:", componentNameInput,
                "Component Group:", componentGroupInput,
                "Component Category:",componentCategoryInput,
                "Client Libs:",createClientLibsChkBx,
                "Full Client Libs:",createFullClientLibsChkBx
        };

        //todo set focus to component name input
        int option = JOptionPane.showConfirmDialog(null, message, "Create", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            this.componentName = componentNameInput.getText();
            this.componentGroup = componentGroupInput.getText();
            this.componentCategory = componentCategoryInput.getText();
            this.createClientLibs = createClientLibsChkBx.isSelected();
            this.createFullClientLibs = createFullClientLibsChkBx.isSelected();
        }

        this.currentDir = getCurrentWorkingDirectory(e);
        createFolder(this.componentName);

        if(this.componentGroup.equals("")){
            this.componentGroup = "no-component-group";
        }

        generateCQDialog();
        generateHTML();
        generateContentXML();
        generateEditConfig();

        if(createClientLibs){
            if (createFullClientLibs) {
                generateFullClientLibs();
            } else {
                generateStandardClientLibs();
            }
        }

        refreshWindow(e);
    }

    private void generateFullClientLibs() {
        this.generateFullClientLibFolders();
        this.generateFullClientLibNodes();

        generateLessFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/site/less");
        generateLessFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/editor/less");

        generateLessTextFileFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/editor");
        generateLessTextFileFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/site");

        generateJSFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/site/js");
        generateJSFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/editor/js");

        generateJSTextFileFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/editor");
        generateJSTextFileFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/site");
    }

    private void generateJSTextFileFromTemplate(String directory) {
        try {
            File newFile = this.writeFileFromTemplate("files/js-txt-template.txt",directory + "/js.txt");
            replaceTextInFile(newFile, "componentName", this.componentName);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateJSFromTemplate(String directory) {
        try {
            File newFile = this.writeFileFromTemplate("files/js-template.txt",directory + "/js.js");
            replaceTextInFile(newFile, "componentName", this.componentName);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateLessTextFileFromTemplate(String directory) {
        try {
            File newFile = this.writeFileFromTemplate("files/css-txt-template.txt",directory + "/css.txt");
            replaceTextInFile(newFile, "componentName", this.componentName);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateLessFromTemplate(String directory) {
        try {
            File newFile = this.writeFileFromTemplate("files/less-template.txt",directory + "/styles.less");
            replaceTextInFile(newFile, "componentName", this.componentName);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateFullClientLibNodes() {
        try {
            File siteNode = this.writeFileFromTemplate("files/client-libs-content-xml-template.txt",this.currentDir + "/" + this.componentName + "/clientlibs/site/_cq_dialog.xml");
            replaceTextInFile(siteNode, "clientLibCategory", this.componentCategory);

            File editorNode = this.writeFileFromTemplate("files/client-libs-content-xml-template.txt",this.currentDir + "/" + this.componentName + "/clientlibs/editor/_cq_dialog.xml");
            replaceTextInFile(editorNode, "clientLibCategory", this.componentCategory);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateFullClientLibFolders() {
        createFolder(this.componentName + "/clientlibs");

        createFolder(this.componentName + "/clientlibs/site");
        createFolder(this.componentName + "/clientlibs/site/js");
        createFolder(this.componentName + "/clientlibs/site/less");

        createFolder(this.componentName + "/clientlibs/editor");
        createFolder(this.componentName + "/clientlibs/editor/js");
        createFolder(this.componentName + "/clientlibs/editor/less");
    }

    private void generateStandardClientLibFolders() {
        createFolder(this.componentName + "/clientlibs/js");
        createFolder(this.componentName + "/clientlibs/less");
    }

    private void generateStandardClientLibs() {
        generateClientLibsNode();
        generateStandardClientLibFolders();

        generateLessFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/less");
        generateLessTextFileFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs");

        generateJSFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/js");
        generateJSTextFileFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs");
    }

    private void generateClientLibsNode() {
        createFolder(this.componentName + "/clientlibs");
        this.generateClientLibsXML();
    }

    private void generateClientLibsXML() {
        try {
            File newFile = this.writeFileFromTemplate("files/client-libs-content-xml-template.txt",this.currentDir + "/" + this.componentName + "/clientlibs/_cq_dialog.xml");
            //todo client lib category
            replaceTextInFile(newFile, "clientLibCategory", this.componentCategory);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateCQDialog() {
        try {
            File newFile = this.writeFileFromTemplate("files/cq_dialog-template.txt",this.currentDir + "/" + this.componentName + "/_cq_dialog.xml");
            replaceTextInFile(newFile, "componentName",this.componentName);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateEditConfig() {
        try {
            File newFile = this.writeFileFromTemplate("files/edit-config-template.txt",this.currentDir + "/" + this.componentName + "/_cq_editConfig.xml");
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateContentXML() {
        try {
            File newFile = this.writeFileFromTemplate("files/content-xml-template.txt",this.currentDir + "/" + this.componentName + "/.content.xml");
            replaceTextInFile(newFile, "componentName",this.componentName);
            replaceTextInFile(newFile, "inputComponentGroup",this.componentGroup);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateHTML() {
        try {
            File newFile = this.writeFileFromTemplate("files/html-template.txt",this.currentDir + "/" + this.componentName + "/" +this.componentName + ".html");
            replaceTextInFile(newFile, "componentName",this.componentName);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void refreshWindow(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        project.getBaseDir().refresh(false,true);
    }

    private void createFile(String name, String fileContent) {
        String path = currentDir + name;
        //this is awful
        BufferedWriter output = null;
        try {
            File file = new File(path);
            output = new BufferedWriter(new FileWriter(file));
            output.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createFolder(String folderName) {
        String path = currentDir + folderName;
        new File(path).mkdirs();
    }

    private String getCurrentWorkingDirectory(AnActionEvent e) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        return file.getPath()+"/";
    }

    private File writeFileFromTemplate(String templateName, String fileName) {
        InputStream targetStream = this.getClass().getResourceAsStream(templateName);

        File populatedFile = new File(fileName);
        try {
            FileUtils.copyInputStreamToFile(targetStream, populatedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return populatedFile;
    }

    private void replaceTextInFile(File file, String word, String replacement) {
        try {
            FileReader fr = new FileReader(file);
            String s;
            String totalStr = "";
            try (BufferedReader br = new BufferedReader(fr)) {

                while ((s = br.readLine()) != null) {
                    totalStr += s + "\n";
                }

                totalStr = totalStr.replaceAll(word, replacement);
                FileWriter fw = new FileWriter(file);
                fw.write(totalStr);
                fw.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}