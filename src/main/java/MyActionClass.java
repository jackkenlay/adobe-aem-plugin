import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

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
            componentName = componentNameInput.getText();
            componentGroup = componentGroupInput.getText();
            createClientLibs = createClientLibsChkBx.isSelected();
            createFullClientLibs = createFullClientLibsChkBx.isSelected();
        }

        this.currentDir = getCurrentWorkingDirectory(e);


        generateCQDialog(componentName);


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

    private void refreshWindow(AnActionEvent e){
        Project project = e.getData(PlatformDataKeys.PROJECT);
        project.getBaseDir().refresh(false,true);
    }

    private void createContentXML(String componentName, String componentGroup) {
        String contentString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jcr:root xmlns:cq=\"http://www.day.com/jcr/cq/1.0\"\n" +
                "          xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"\n" +
                "          jcr:primaryType=\"cq:Component\"\n" +
                "          jcr:title=\""+componentName+"\"\n" +
                "          componentGroup=\""+componentGroup+"\"/>";
        createFile(componentName + "/.content.xml", contentString);
    }

    private void createEditConfig(String componentName) {
        String editConfigText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jcr:root xmlns:cq=\"http://www.day.com/jcr/cq/1.0\"\n" +
                "          xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"\n" +
                "          jcr:primaryType=\"cq:EditConfig\"/>";

        createFile(componentName + "/_cq_editConfig.xml", editConfigText);
    }

    private void createHTML(String componentName) {
        String htmlText = this.getHTMLText(componentName);
        createFile(componentName + "/"+componentName+".html", htmlText);
    }

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

    private String getHTMLText(String componentName){
        return "<div id=\"component-"+componentName+"\">\n" +
               "    ${properties.text || \"Hello\"}\n" +
               "</div>";
    }

    private String getCurrentWorkingDirectory(AnActionEvent e) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        //VirtualFile folder = file.getParent();

        System.out.println("getCurrentWorkingDirectory returning: "+file.getPath());
        return file.getPath()+"/";
    }

    private String getCQDialogText(String componentName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jcr:root xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"\n" +
                "	xmlns:sling=\"http://sling.apache.org/jcr/sling/1.0\" jcr:primaryType=\"nt:unstructured\"\n" +
                "	jcr:title=\""+componentName+"\" sling:resourceType=\"cq/gui/components/authoring/dialog\">\n" +
                "	<content jcr:primaryType=\"nt:unstructured\"\n" +
                "		sling:resourceType=\"granite/ui/components/coral/foundation/fixedcolumns\"\n" +
                "		margin=\"{Boolean}true\">\n" +
                "		<items jcr:primaryType=\"nt:unstructured\">\n" +
                "			<column jcr:primaryType=\"nt:unstructured\"\n" +
                "				sling:resourceType=\"granite/ui/components/coral/foundation/container\">\n" +
                "				<items jcr:primaryType=\"nt:unstructured\">\n" +
                "					<text jcr:primaryType=\"nt:unstructured\"\n" +
                "						sling:resourceType=\"granite/ui/components/coral/foundation/form/textfield\"\n" +
                "						emptyText=\"My Text\"\n" +
                "						fieldDescription=\"Add in custom text\"\n" +
                "						fieldLabel=\"Custom text\" name=\"./text\" />\n" +
                "				</items>\n" +
                "			</column>\n" +
                "		</items>\n" +
                "	</content>\n" +
                "</jcr:root>";
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

    private void generateCQDialog(String componentName){
        //todo this is hidious - refactor
        try {
//            InputStream targetStream = this.getClass().getResourceAsStream("files/cq_dialog-template.txt");
//
//            File populatedFile = new File(this.currentDir+"test.txt");
//            FileUtils.copyInputStreamToFile(targetStream, populatedFile);

            File newFile = this.writeFileFromTemplate("files/cq_dialog-template.txt",this.currentDir+"test.xml");

            try {
                FileReader fr = new FileReader(newFile);
                String s;
                String totalStr = "";
                try (BufferedReader br = new BufferedReader(fr)) {

                    while ((s = br.readLine()) != null) {
                        totalStr += s + "\n";
                    }

                    totalStr = totalStr.replaceAll("componentName", componentName);
                    FileWriter fw = new FileWriter(newFile);
                    fw.write(totalStr);
                    fw.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        } catch (Exception e) {
            //Simple exception handling, replace with what's necessary for your use case!
            throw new RuntimeException("Generating file failed", e);
        }

        //cut the dialog to the new directory
    }

    public void duplicateCQDialogTemplate(){
        //ClassLoader classLoader = getClass().getClassLoader();

//        URL test = this.getClass().getResource("files/cq_dialog-template.txt");
//        File testFile = new File(test.toURI());

//        System.out.println("Class:");
//        System.out.println(this.getClass());
//        System.out.println("URL: " + test);



//        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//        URL itest2 = classloader.getResource("../resources/files/cq_dialog-template.txt");
//        System.out.println("URL2: " + itest2);
//
//        System.out.println("here");
//        String fileName = "cq_dialog-template.txt";
//        File file = null;
//        try{
//            file = new File(classloader.getResource(fileName).getFile());
//        }catch (Exception e){
//            System.out.println("file not found:P " + e.getMessage());
//        }
//
//        System.out.println("here3");

        //File is found
       // System.out.println("File Found : " + file.exists());

///Users/jackkenlay/personal/adobe-aem-plugin/src/main/resources/files/cq_dialog-template.txt
        //Read File Content
        //String content = new String(Files.readAllBytes(file.toPath()));
        //System.out.println(content);


//        File template = new File(classLoader.getResource("/resources/files/cq_dialog-template.txt").getFile());
//        String tempFilePath = this.currentDir + "/cq_dialog.txt";
//        File cqdialog = new File(tempFilePath);

        //File cqdialog = new File("cq_dialog.txt");
//        try {
//            this.copyFile(template,cqdialog);
//        } catch (Exception e) {
//            System.out.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    private void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }
}