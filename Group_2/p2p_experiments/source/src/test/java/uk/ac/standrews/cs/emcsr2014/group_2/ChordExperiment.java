package uk.ac.standrews.cs.emcsr2014.group_2;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerManager;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.ChordConfiguration;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.ExponentialIntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.RandomKeySupplier;
import uk.ac.standrews.cs.trombone.event.environment.Workload;
import uk.ac.standrews.cs.trombone.event.util.SequentialPortNumberSupplier;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@RunWith(Parameterized.class)
public class ChordExperiment {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChordExperiment.class);
    static final Scenario scenario = new Scenario("chord_11", 1413);
    private static final SequentialPortNumberSupplier PORT_NUMBER_PROVIDER = new SequentialPortNumberSupplier(55000);
    private static final ExponentialIntervalGenerator THREE_MINUTES_EXPONENTIAL = new ExponentialIntervalGenerator(new Duration(3, TimeUnit.MINUTES), 7376);
    private static final RandomKeySupplier LOOKUP_TARGET_KEY_SUPPLIER = new RandomKeySupplier(889);
    private static final ExponentialIntervalGenerator ONE_SECOND_EXPONENTIAL = new ExponentialIntervalGenerator(new Duration(500, TimeUnit.MILLISECONDS), 192);
    private static final ChordConfiguration CHORD_CONFIGURATION = new ChordConfiguration(3, Key.TWO, 3, 5, TimeUnit.SECONDS, 100);
    private static final Workload WORKLOAD = new Workload(LOOKUP_TARGET_KEY_SUPPLIER, ONE_SECOND_EXPONENTIAL);
    private static final Churn CHURN = new Churn(THREE_MINUTES_EXPONENTIAL, THREE_MINUTES_EXPONENTIAL);
    private static final Duration WORKER_DEPLOYMENT_TIMEOUT = new Duration(5, TimeUnit.MINUTES);
    private static final OpenSSHKeyFile SSH_KEY_FILE;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
    private static final Duration ADDITIONAL_WAIT = new Duration(10, TimeUnit.MINUTES);
    private static final String EXPERIMENT_HOST_NAMES_PROPERTY_KEY = "experiment.host_names";
    private static final String HOSTS_PROPERTY_VALUE = System.getProperty(EXPERIMENT_HOST_NAMES_PROPERTY_KEY);
    private static final List<String> AVAILABLE_HOST_NAMES = getHostNames();

    static {

        SSH_KEY_FILE = new OpenSSHKeyFile();
        SSH_KEY_FILE.init(new File(System.getProperty("user.home") + File.separator + ".ssh", "id_rsa"));
        // final File location = new File(System.getProperty("user.home") + File.separator + "credentials", "p2p_experiment.key");
        // SSH_KEY_FILE.init(location);

        LOGGER.info("key exists? {}", location.isFile());

        scenario.setLookupRetryCount(5);
        scenario.setExperimentDuration(new Duration(30, TimeUnit.MINUTES));
        scenario.setObservationInterval(new Duration(10, TimeUnit.SECONDS));
        scenario.setPeerKeyProvider(new RandomKeySupplier(78218713));

        for (String host_name : AVAILABLE_HOST_NAMES) {
            scenario.addHost(host_name, 1, PORT_NUMBER_PROVIDER, CHURN, WORKLOAD, CHORD_CONFIGURATION);
        }
                           try {
                               System.out.println(InetAddress.getLocalHost()
                                       .getHostName());
                           }catch (Exception e){

                           }
    }

    private static final int REPETITIONS = 5;

    @Parameterized.Parameters
    public static List<Object[]> data() {

        return Arrays.asList(new Object[REPETITIONS][0]);
    }

    private WorkerNetwork network;
    private WorkerManager manager;

    @Before
    public void setUp() throws Exception {

        LOGGER.info("starting experiment...");
        network = new WorkerNetwork(56001);
        manager = network.getWorkerManager();
        manager.setWorkerJVMArguments("-D" + EXPERIMENT_HOST_NAMES_PROPERTY_KEY + '=' + HOSTS_PROPERTY_VALUE + " -Xmx1G");
        manager.setWorkerDeploymentTimeout(WORKER_DEPLOYMENT_TIMEOUT);
        network.addCurrentJVMClasspath();
        network.setAutoDeployEnabled(false);

        LOGGER.info("preparing worker network of {} hosts...", AVAILABLE_HOST_NAMES.size());
        for (String host_name : AVAILABLE_HOST_NAMES) {
            network.add(new SSHHost(host_name, "ubuntu", new AuthPublickey(SSH_KEY_FILE)));
            LOGGER.info("added host {}", host_name);
        }

        LOGGER.info("deploying workers...");
        network.deployAll();
        LOGGER.info("awaiting running state...");
        network.awaitAnyOfStates(ApplicationState.RUNNING);
        LOGGER.info("worker network is up and running.");
    }

    @Test
    public void doExperiment() throws Exception {

        LOGGER.info("starting execution of scenario {}...", scenario.getName());

        final Map<Host, Future<String>> host_event_executions = new HashMap<>();

        for (ApplicationDescriptor descriptor : network) {

            final Worker worker = descriptor.getApplicationReference();
            final Host host = descriptor.getHost();
            final int host_index = getHostIndexByName(host.getName());

            LOGGER.info("submitting job to {} indexed as {}", host, host_index);
            final Future<String> future_event_execution = worker.submit(new ScenarioExecutionJob());
            host_event_executions.put(host, future_event_execution);
        }

        final Path repetitions = ScenarioUtils.getScenarioRepetitionsHome(scenario.getName());
        assureRepetitionsDirectoryExists(repetitions);
        final Path observations = newObservationsPath(repetitions);
        LOGGER.info("collected observations will be stored at {}", observations.toAbsolutePath());

        Exception error = null;

        try (FileSystem observations_fs = FileSystemUtils.newZipFileSystem(observations, true)) {

            final Path root_observations = observations_fs.getPath(observations_fs.getSeparator());
            for (Map.Entry<Host, Future<String>> host_event_entry : host_event_executions.entrySet()) {

                final Host host = host_event_entry.getKey();
                final Future<String> future_event_execution = host_event_entry.getValue();

                try {
                    final Duration timeout = ADDITIONAL_WAIT.add(scenario.getExperimentDuration());
                    final String results_path = future_event_execution.get(timeout.getLength(), timeout.getTimeUnit());
                    LOGGER.info("successfully finished executing events on host {} - {}", host, results_path);

                    final Path destination = Files.createTempDirectory(host.getName());
                    host.download(results_path, destination.toFile());

                    LOGGER.info("downloaded observations from host {} to {}", host.getName(), destination);

                    final File zip = new File(destination.toFile(), FilenameUtils.getName(results_path));
                    final int host_index = getHostIndexByName(host.getName());
                    final Path local_observations = root_observations.resolve(String.valueOf(host_index));

                    Files.createDirectories(local_observations);

                    try (final FileSystem fileSystem = FileSystemUtils.newZipFileSystem(zip.getAbsolutePath(), false)) {
                        FileSystemUtils.copyRecursively(fileSystem.getPath("/"), local_observations);
                    }
                    FileUtils.deleteQuietly(destination.toFile());
                }
                catch (InterruptedException | ExecutionException | TimeoutException | CancellationException e) {
                    final Throwable cause = e.getCause();
                    LOGGER.error("Event execution on host {} failed due to {}", host, cause != null ? cause : e);
                    LOGGER.error("Failure details", cause != null ? cause : e);
                    error = e;
                    break;
                }
            }
        }
        if (error != null) {
            Files.deleteIfExists(observations);
            throw error;
        }
    }

    @After
    public void tearDown() throws Exception {

        LOGGER.info("shutting down worker network...");
        network.shutdown();
    }

    private Integer getHostIndexByName(final String host_name) {

        LOGGER.info("looking up host {}", host_name);
        return ScenarioExecutionJob.getHostIndex(host_name);
    }

    private static List<String> getHostNames() {

        LOGGER.info("loading host names from {} system property", EXPERIMENT_HOST_NAMES_PROPERTY_KEY);
        LOGGER.info("given host names: {}", HOSTS_PROPERTY_VALUE);
        return new CopyOnWriteArrayList<>(HOSTS_PROPERTY_VALUE.trim()
                .split(","));
    }

    static synchronized Path newObservationsPath(final Path repetitions) {

        return repetitions.resolve(DATE_FORMAT.format(new Date()) + ".zip");
    }

    static void assureRepetitionsDirectoryExists(final Path repetitions) throws IOException {

        if (!Files.isDirectory(repetitions)) {
            Files.createDirectories(repetitions);
        }
    }
}
