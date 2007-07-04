package org.apache.maven.plugin.war.packaging;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Handles the artifacts that needs to be packaged in the web application.
 *
 * @author Stephane Nicoll
 */
public class ArtifactsPackagingTask
    extends AbstractWarPackagingTask
{

    public static final String LIB_PATH = "WEB-INF/lib";

    public static final String TLD_PATH = "WEB-INF/tld";

    public static final String SERVICES_PATH = "WEB-INF/services";

    private final Set artifacts;


    public ArtifactsPackagingTask( Set artifacts )
    {
        this.artifacts = artifacts;
    }


    public void performPackaging( WarPackagingContext context )
        throws MojoExecutionException
    {

        final ScopeArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_RUNTIME );
        final List duplicates = findDuplicates( context, artifacts );

        for ( Iterator iter = artifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            String targetFileName = getArtifactFinalName( context, artifact );

            context.getLogger().debug( "Processing: " + targetFileName );

            if ( duplicates.contains( targetFileName ) )
            {
                context.getLogger().debug( "Duplicate found: " + targetFileName );
                targetFileName = artifact.getGroupId() + "-" + targetFileName;
                context.getLogger().debug( "Renamed to: " + targetFileName );
            }

            if ( !artifact.isOptional() && filter.include( artifact ) )
            {
                try
                {
                    String type = artifact.getType();
                    if ( "tld".equals( type ) )
                    {
                        copyFile( context, artifact.getFile(), TLD_PATH + targetFileName );
                    }
                    else if ( "aar".equals( type ) )
                    {
                        copyFile( context, artifact.getFile(), SERVICES_PATH + targetFileName );
                    }
                    else if ( "jar".equals( type ) || "ejb".equals( type ) || "ejb-client".equals( type ) ||
                        "test-jar".equals( type ) )
                    {
                        copyFile( context, artifact.getFile(), LIB_PATH + targetFileName );
                    }
                    else if ( "par".equals( type ) )
                    {
                        targetFileName = targetFileName.substring( 0, targetFileName.lastIndexOf( '.' ) ) + ".jar";
                        copyFile( context, artifact.getFile(), LIB_PATH + targetFileName );
                    }
                    else
                    {
                        context.getLogger().debug(
                            "Artifact of type[" + type + "] is not supported, ignoring[" + artifact + "]" );
                    }
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Failed to copy file for artifact[" + artifact + "]", e );
                }
            }
        }
    }

    /**
     * Searches a set of artifacts for duplicate filenames and returns a list
     * of duplicates.
     *
     * @param context   the packaging context
     * @param artifacts set of artifacts
     * @return List of duplicated artifacts as bundling file names
     */
    private List findDuplicates( WarPackagingContext context, Set artifacts )
    {
        List duplicates = new ArrayList();
        List identifiers = new ArrayList();
        for ( Iterator iter = artifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            String candidate = getArtifactFinalName( context, artifact );
            if ( identifiers.contains( candidate ) )
            {
                duplicates.add( candidate );
            }
            else
            {
                identifiers.add( candidate );
            }
        }
        return duplicates;
    }
}
