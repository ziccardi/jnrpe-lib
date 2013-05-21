package it.jnrpe.plugins.test;

import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.client.JNRPEClient;
import it.jnrpe.commands.CommandDefinition;
import it.jnrpe.commands.CommandOption;
import it.jnrpe.commands.CommandRepository;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.utils.PluginRepositoryUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for the checktomcat plugin.
 *
 * @author Massimiliano Ziccardi
 */
@Test
public class CheckTomcatTest implements Constants {

    /**
     * The full path to the already existing tomcat installation.
     * If this variable is null, than tomcat gets automatically
     * downloaded during test execution phase.
     */
    private String existingTomcatInstance = null;

    /**
     * The tomcat download url.
     */
    private static final String TOMCAT_DOWNLOAD_URL =
            "http://apache.fastbull.org/tomcat/tomcat-7/v7.0.40"
                    + "/bin/apache-tomcat-7.0.40.zip";

    /**
     * The tomcat container.
     */
    private InstalledLocalContainer container = null;

    /**
     * Writes the tomcat-users.xml giving all the privileges to the user
     * tomcat/tomcat.
     *
     * @param f
     *            The destination file
     * @throws IOException
     *             -
     */
    private void enableTomcatConsole(final File f) throws IOException {
        String xml =
                "<?xml version='1.0' encoding='utf-8'?>"
                        + "<tomcat-users>"
                        + "<role rolename=\"manager-gui\"/>"
                        + "<role rolename=\"manager-script\"/>"
                        + "<role rolename=\"manager-jmx\"/>"
                        + "<role rolename=\"manager-status\"/>"
                        + "<user username=\"tomcat\" password=\"tomcat\" "
                        + "roles=\"manager-gui,manager-script,manager-jmx,"
                        + "manager-status\"/>"
                        + "</tomcat-users>";

        FileUtils.writeStringToFile(f, xml);
    }

    /**
     * Starts tomcat.
     */
    @BeforeClass
    public final void setup() {
        try {
            ClassLoader cl = CheckTomcatTest.class.getClassLoader();

            PluginDefinition checkTomcat =
                    PluginRepositoryUtil.parseXmlPluginDefinition(cl,
                            cl.getResourceAsStream("check_tomcat_plugin.xml"));

            SetupTest.getPluginRepository().addPluginDefinition(checkTomcat);

            String tomcatHome = null;

            if (existingTomcatInstance == null) {
                File tomcatDir = new File("./target/tomcat");

                tomcatDir.mkdirs();

                ZipURLInstaller installer =
                        new ZipURLInstaller(new URL(TOMCAT_DOWNLOAD_URL));
                installer.setExtractDir(tomcatDir.getAbsolutePath());
                installer.install();
                tomcatHome = installer.getHome();
            } else {
                tomcatHome = existingTomcatInstance;
            }

            DefaultConfigurationFactory factory =
                    new DefaultConfigurationFactory();
            factory.registerConfiguration("tomcat7xJnrpeTest",
                    ContainerType.INSTALLED,
                    ConfigurationType.STANDALONE, Tomcat7xConf.class);

            LocalConfiguration configuration =
                    (LocalConfiguration) factory
                            .createConfiguration(
                                    "tomcat7xJnrpeTest",
                                    ContainerType.INSTALLED,
                                    ConfigurationType.STANDALONE);

            container =
                    (InstalledLocalContainer) new DefaultContainerFactory()
                            .createContainer(
                                    "tomcat7x", ContainerType.INSTALLED,
                                    configuration);

            container.setHome(tomcatHome);

            ((Tomcat7xConf) configuration).realConfigure(container);
            enableTomcatConsole(new File(new File(configuration.getHome(),
                    "conf"), "tomcat-users.xml"));

            container.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Executes the test.
     * @throws Exception -
     */
    @Test
    public final void checkTomcatOK() throws Exception {
        CommandRepository cr = SetupTest.getCommandRepository();

        cr.addCommandDefinition(new CommandDefinition("CHECK_TOMCAT",
                "CHECK_TOMCAT")
                .addArgument(new CommandOption("hostname", "$ARG1$"))
                .addArgument(new CommandOption("port", "$ARG2$"))
                .addArgument(new CommandOption("username", "$ARG3$"))
                .addArgument(new CommandOption("password", "$ARG4$"))
                );

        JNRPEClient client = new JNRPEClient(BIND_ADDRESS, JNRPE_PORT, false);
        ReturnValue ret =
                client.sendCommand("CHECK_TOMCAT",
                        "127.0.0.1", "8080", "tomcat", "tomcat");

        Assert.assertEquals(ret.getStatus(), Status.OK, ret.getMessage());
    }

    /**
     * Stop tomcat.
     */
    @AfterClass
    public final void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

}
