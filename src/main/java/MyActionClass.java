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
    private boolean createClientLibs;
    private boolean createFullClientLibs;

    @Override
    public void actionPerformed(AnActionEvent e) {

        /*
         * TO DO
         * Option for 'flat' or editor/site clientlibs
         * Set focus of input text field
         * Style inputdialog
         * Configurable settings
         *  - default component group
         *  - if null, then can save new entry
         *  - default html/less/js templates (hard)
         * Add in more default nodes in CQDialog so you can go in and delete it after
         * Ensure dialog is smooth experience with keyboard
         * Test export
         * Round off ReadME
         */

        //componentName = JOptionPane.showInputDialog(null,"Enter component name:","my-component");


        JTextField componentNameInput = new JTextField();
        JTextField componentGroupInput = new JTextField();

        JCheckBox createClientLibsChkBx = new JCheckBox();
        createClientLibsChkBx.setSelected(true);

        JCheckBox createFullClientLibsChkBx = new JCheckBox();
        createFullClientLibsChkBx.setSelected(true);

        Object[] message = {
                "Component Name:", componentNameInput,
                "Component Group:", componentGroupInput,
                "Client Libs:",createClientLibsChkBx,
                "Full Client Libs:",createFullClientLibsChkBx
        };

        //todo set focus to component name input
        int option = JOptionPane.showConfirmDialog(null, message, "Create", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            this.componentName = componentNameInput.getText();
            this.componentGroup = componentGroupInput.getText();
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

        //todo templates
        //edit config
        //client lib category
        //client lib folders
        //client libs txt files
        //client libs SASS
        //client libs LESS
        //client libs JS
        //have checker for less or SASS

        /*---------------------------------------------------
        // create component directory
        createFolder(componentName);

        // create .content.xml
        createContentXML(componentName,componentGroup);

        // create CQ Dialog
        String content = getCQDialogText(componentName);
        createFile(componentName + "/_cq_dialog.xml", content);

        // create JS, LESS configs/files
        if(createClientLibs){
            String clientLibsCategory = componentGroup;
            if (createFullClientLibs) {
                createFullClientLibs(componentName, clientLibsCategory);
            } else {
                createClientLibs(componentName, clientLibsCategory);
            }
        }

        // create EditConfig
        createEditConfig(componentName);

        // create HTML
        createHTML(componentName);
        ----------------------------------------------------*/

        refreshWindow(e);
    }

    private void generateFullClientLibs() {
        this.generateFullClientLibFolders();
        this.generateFullClientLibNodes();
        generateLessFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/site/less");
        generateLessFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/editor/less");

        generateJSFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/site/js");
        generateJSFromTemplate(this.currentDir+"/"+this.componentName+"/clientlibs/editor/js");
    }

    private void generateJSFromTemplate(String directory) {
        try {
            File newFile = this.writeFileFromTemplate("files/js-template.txt",directory + "/js.js");
            replaceTextInFile(newFile, "componentName", this.componentName);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateLessFromTemplate(String directory) {
        try {
            File newFile = this.writeFileFromTemplate("files/less-template.txt",directory + "/styles.less");
            //todo client lib category
            replaceTextInFile(newFile, "componentName", this.componentName);
        } catch (Exception e) {
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateFullClientLibNodes() {
        try {
            File siteNode = this.writeFileFromTemplate("files/client-libs-content-xml-template.txt",this.currentDir + "/" + this.componentName + "/clientlibs/site/_cq_dialog.xml");
            //todo client lib category
            replaceTextInFile(siteNode, "clientLibCategory", this.componentGroup);

            File editorNode = this.writeFileFromTemplate("files/client-libs-content-xml-template.txt",this.currentDir + "/" + this.componentName + "/clientlibs/editor/_cq_dialog.xml");
            replaceTextInFile(editorNode, "clientLibCategory", this.componentGroup);
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

    private void generateStandardClientLibs() {
        generateClientLibsNode();
    }

    private void generateClientLibsNode() {
        createFolder(this.componentName + "/clientlibs");
        this.generateClientLibsXML();
    }

    private void generateClientLibsXML() {
        try {
            File newFile = this.writeFileFromTemplate("files/client-libs-content-xml-template.txt",this.currentDir + "/" + this.componentName + "/clientlibs/_cq_dialog.xml");
            //todo client lib category
            replaceTextInFile(newFile, "clientLibCategory", this.componentGroup);
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

    private String getDefaultJavaScriptContent() {
        return "(function(){\n" +
                "    console.log('Client libs JS Loaded');\n" +
                "})();";
    }

    private String getDefaultLessContent(String componentName) {
        return "#content-"+componentName+"{\n" +
                "    \n" +
                "}";
    }

    private void createFile(String name, String fileContent) {
        String path = currentDir + name;

        BufferedWriter output = null;
        try {
            File file = new File(path);
            output = new BufferedWriter(new FileWriter(file));
            output.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //this is awful
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
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