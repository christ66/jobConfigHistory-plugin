package hudson.plugins.jobConfigHistory;

import hudson.model.Action;
import hudson.model.Hudson;
import hudson.plugins.jobConfigHistory.SideBySideView.Line;
import hudson.security.AccessControlled;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.Stapler;


import difflib.DiffUtils;
import difflib.Patch;
import difflib.StringUtills;
import java.util.Arrays;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Implements some basic methods needed by the
 * {@link JobConfigHistoryRootAction} and {@link JobConfigHistoryProjectAction}.
 *
 * @author mfriedenhagen
 */
public abstract class JobConfigHistoryBaseAction implements Action {

    /**
     * The hudson instance.
     */
    private final Hudson hudson;

    /**
     * Set the {@link Hudson} instance.
     */
    public JobConfigHistoryBaseAction() {
        hudson = Hudson.getInstance();
    }

    /**
     * For tests only.
     *
     * @param hudson injected jenkins
     */
    JobConfigHistoryBaseAction(Hudson hudson) {
        this.hudson = hudson;
    }

    @Override
    public final String getDisplayName() {
        return Messages.displayName();
    }

    @Override
    public String getUrlName() {
        return JobConfigHistoryConsts.URLNAME;
    }

    /**
     * Returns how the config file should be formatted in configOutput.jelly: as plain text or xml.
     *
     * @return "plain" or "xml"
     */
    public final String getOutputType() {
        if (("xml").equalsIgnoreCase(getRequestParameter("type"))) {
            return "xml";
        }
        return "plain";
    }

    /**
     * Checks the url parameter 'timestamp' and returns true if it is parseable as a date.
     * @param timestamp Timestamp of config change.
     * @return True if timestamp is okay.
     */
    protected boolean checkTimestamp(String timestamp) {
        if (timestamp == null || "null".equals(timestamp)) {
            return false;
        }
        PluginUtils.parsedDate(timestamp);
        return true;
    }

    /**
     * Returns the parameter named {@code parameterName} from current request.
     *
     * @param parameterName
     *            name of the parameter.
     * @return value of the request parameter or null if it does not exist.
     */
    protected String getRequestParameter(final String parameterName) {
        return getCurrentRequest().getParameter(parameterName);
    }

    /**
     * See whether the current user may read configurations in the object
     * returned by
     * {@link JobConfigHistoryBaseAction#getAccessControlledObject()}.
     */
    protected abstract void checkConfigurePermission();

    /**
     * Returns whether the current user may read configurations in the object
     * returned by
     * {@link JobConfigHistoryBaseAction#getAccessControlledObject()}.
     *
     * @return true if the current user may read configurations.
     */
    protected abstract boolean hasConfigurePermission();

    /**
     * Returns the hudson instance.
     *
     * @return the hudson
     */
    protected Hudson getHudson() {
        return hudson;
    }

    /**
     * Returns the object for which we want to provide access control.
     *
     * @return the access controlled object.
     */
    protected abstract AccessControlled getAccessControlledObject();

    /**
     * Returns side-by-side (i.e. human-readable) diff view lines.
     *
     * @param diffLines Unified diff as list of Strings.
     * @return Nice and clean diff as list of single Lines.
     * @throws IOException
     *             if reading one of the config files does not succeed.
     */
    public final List<Line> getDiffLines(List<String> diffLines) throws IOException {
        return new GetDiffLines(diffLines).get();
    }

    /**
     * Returns a unified diff between two string arrays.
     *
     * @param file1
     *            first config file.
     * @param file2
     *            second config file.
     * @param file1Lines
     *            the lines of the first file.
     * @param file2Lines
     *            the lines of the second file.
     * @return unified diff
     */
    protected final String getDiffAsString(final File file1, final File file2,
            final String[] file1Lines, final String[] file2Lines) {
        final Patch patch = DiffUtils.diff(Arrays.asList(file1Lines), Arrays.asList(file2Lines));
        final List<String> unifiedDiff = DiffUtils.generateUnifiedDiff(
                file1.getPath(), file2.getPath(), Arrays.asList(file1Lines), patch, 3);
        return StringUtills.join(unifiedDiff, "\n") + "\n";
    }

    /**
     * Overridable for tests.
     *
     * @return current request
     */
    StaplerRequest getCurrentRequest() {
        return Stapler.getCurrentRequest();
    }

    /**
     * Returns the plugin for tests.
     *
     * @return plugin
     */
    JobConfigHistory getPlugin() {
        return PluginUtils.getPlugin();
    }

    /**
     * For tests.
     *
     * @return historyDao
     */
    HistoryDao getHistoryDao() {
        return PluginUtils.getHistoryDao();
    }

}
