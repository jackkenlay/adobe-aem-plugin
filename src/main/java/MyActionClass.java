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


        generateCQDialog();
        generateHTML();
        generateContentXML();

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

    private void generateContentXML() {
        try {
            File newFile = this.writeFileFromTemplate("files/content-xml-template.txt",this.currentDir + this.componentName + ".xml");
            replaceTextInFile(newFile, "componentName",this.componentName);
            replaceTextInFile(newFile, "componentGroup",this.componentGroup);
        } catch (Exception e) {
            //Simple exception handling, replace with what's necessary for your use case!
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void generateHTML(){
        try {
            File newFile = this.writeFileFromTemplate("files/html-template.txt",this.currentDir + this.componentName + ".html");
            replaceTextInFile(newFile, "componentName",this.componentName);
        } catch (Exception e) {
            //Simple exception handling, replace with what's necessary for your use case!
            throw new RuntimeException("Generating file failed", e);
        }
    }

    private void refreshWindow(AnActionEvent e){
        Project project = e.getData(PlatformDataKeys.PROJECT);
        project.getBaseDir().refresh(false,true);
    }

//    private void createContentXML(String componentName, String componentGroup) {
//        String contentString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                "<jcr:root xmlns:cq=\"http://www.day.com/jcr/cq/1.0\"\n" +
//                "          xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"\n" +
//                "          jcr:primaryType=\"cq:Component\"\n" +
//                "          jcr:title=\""+componentName+"\"\n" +
//                "          componentGroup=\""+componentGroup+"\"/>";
//        createFile(componentName + "/.content.xml", contentString);
//    }

    private void createEditConfig(String componentName) {
        String editConfigText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jcr:root xmlns:cq=\"http://www.day.com/jcr/cq/1.0\"\n" +
                "          xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"\n" +
                "          jcr:primaryType=\"cq:EditConfig\"/>";

        createFile(componentName + "/_cq_editConfig.xml", editConfigText);
    }

//    private void createHTML(String componentName) {
//        String htmlText = this.getHTMLText(componentName);
//        createFile(componentName + "/"+componentName+".html", htmlText);
//    }

    private void createFullClientLibs(String componentName, String clientLibCategory) {
        //doesnt make editor LESS OR JS

        String clientLibsContentXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jcr:root xmlns:cq=\"http://www.day.com/jcr/cq/1.0\"\n" +
                "          xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"\n" +
                "          jcr:primaryType=\"cq:ClientLibraryFolder\"\n" +
                "          categories=\""+clientLibCategory+"\"/>";

        // create clientLibs File
        createFolder(componentName + "/clientlibs");

        //site client libs
        createFolder(componentName + "/clientlibs/site");
        createFile(componentName + "/clientlibs/site/.content.xml", clientLibsContentXML);
        createFolder(componentName + "/clientlibs/site/js");
        createFolder(componentName + "/clientlibs/site/less");

        String lessSiteFileText = "#base=less \n\n" + componentName + ".less";
        createFile(componentName + "/clientlibs/site/css.txt", lessSiteFileText);
        String lessFileContent = getDefaultLessContent(componentName);
        createFile(componentName + "/clientlibs/site/less/"+componentName+".less", lessFileContent);

        String jsSiteFileText = "#base=js \n\n" + componentName + ".js";
        createFile(componentName + "/clientlibs/site/js.txt", jsSiteFileText);

        String javaScriptText = this.getDefaultJavaScriptContent();
        createFile(componentName + "/clientlibs/site/js/"+componentName+".js", javaScriptText);


        //editor client libs
        createFolder(componentName + "/clientlibs/editor");
        createFile(componentName + "/clientlibs/editor/.content.xml", clientLibsContentXML);
        createFolder(componentName + "/clientlibs/editor/less");
        createFolder(componentName + "/clientlibs/editor/js");

        String lessFileText = "#base=less \n\n" + componentName + ".less";
        createFile(componentName + "/clientlibs/editor/css.txt", lessFileText);
        String lessEditorFileContent = getDefaultLessContent(componentName);
        createFile(componentName + "/clientlibs/editor/less/"+componentName+".less", lessEditorFileContent);

        String jsFileText = "#base=js \n\n" + componentName + ".js";
        createFile(componentName + "/clientlibs/editor/js.txt", jsFileText);

        String javaEditorScriptText = this.getDefaultJavaScriptContent();
        createFile(componentName + "/clientlibs/editor/js/"+componentName+".js", javaEditorScriptText);

    }


    /*
        Refactoring:

        Make a hashmap of the regex, ie replace "a" with "b"
            {
                "componentName":"(String from input box"),
                "componentGroup":"(String from input box")
            }

        them make a function, createFileFromTemplate('new File Name','template-file-name', hashmap of things to replace);

        function createClientLibFolderStructure

        function createClientLibFullFolderStructure

        function writeComponentFiles

        handle Exceptions
     */



    private void createClientLibs(String componentName, String clientLibCategory) {
        //doesnt make editor LESS OR JS

        String clientLibsContentXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jcr:root xmlns:cq=\"http://www.day.com/jcr/cq/1.0\"\n" +
                "          xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"\n" +
                "          jcr:primaryType=\"cq:ClientLibraryFolder\"\n" +
                "          categories=\""+clientLibCategory+"\"/>";

        // create clientLibs File
        createFolder(componentName + "/clientlibs");

        //site client libs
        createFile(componentName + "/clientlibs/.content.xml", clientLibsContentXML);
        createFolder(componentName + "/clientlibs/js");
        createFolder(componentName + "/clientlibs/less");

        String lessSiteFileText = "#base=less \n\n" + componentName + ".less";
        createFile(componentName + "/clientlibs/css.txt", lessSiteFileText);
        String lessFileContent = getDefaultLessContent(componentName);
        createFile(componentName + "/clientlibs/less/"+componentName+".less", lessFileContent);

        String jsSiteFileText = "#base=js \n\n" + componentName + ".js";
        createFile(componentName + "/clientlibs/js.txt", jsSiteFileText);

        String javaScriptText = this.getDefaultJavaScriptContent();
        createFile(componentName + "/clientlibs/js/"+componentName+".js", javaScriptText);

    }

    private String getDefaultJavaScriptContent(){
        return "(function(){\n" +
                "    console.log('Client libs JS Loaded');\n" +
                "})();";
    }

    private String getDefaultLessContent(String componentName){
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

//    private String getHTMLText(String componentName){
//        return "<div id=\"component-"+componentName+"\">\n" +
//               "    ${properties.text || \"Hello\"}\n" +
//               "</div>";
//    }

    private String getCurrentWorkingDirectory(AnActionEvent e) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        return file.getPath()+"/";
    }

    private File writeFileFromTemplate(String templateName, String fileName){
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

    private void generateCQDialog() {
        //todo this is hidious - refactor
        try {
            File newFile = this.writeFileFromTemplate("files/cq_dialog-template.txt",this.currentDir+"test.xml");
            replaceTextInFile(newFile, "componentName",this.componentName);
        } catch (Exception e) {
            //Simple exception handling, replace with what's necessary for your use case!
            throw new RuntimeException("Generating file failed", e);
        }
    }
}