## BEGIN PROJECT BUILD PROPERTIES ##

# this property allows ant-contrib and pde-svn-plugin to be fetched and installed automatically for you
thirdPartyDownloadLicenseAcceptance="I accept"

projectid=jbosstools.jmx
zipPrefix=JMX
incubation=
buildType=N
version=1.1.0

mainFeatureToBuildID=org.jboss.tools.jmx.sdk.feature
testFeatureToBuildID=org.jboss.tools.jmx.tests.feature

build.steps=buildUpdate,buildTests,generateDigests,test,publish,cleanup

# JBoss Hudson Variables defined in /home/hudson/config_repository/resources/common.variables
JAVA_HOME=${JAVA_HOME_PARENT}
JAVA14_HOME=${JAVA_HOME_PARENT}
JAVA50_HOME=${JAVA_HOME_PARENT}
JAVA60_HOME=${JAVA_HOME_PARENT}

dependencyURLs=\
http://repository.jboss.org/eclipse/galileo/eclipse-SDK-3.5.1-linux-gtk.tar.gz

# use precompiled binaries from latest build as input to this build
repositoryURLs=http://download.eclipse.org/releases/galileo/,http://download.jboss.org/jbosstools/updates/nightly/trunk/
pluginIDsToInstall=org.jboss.tools.vpe.resref+org.jboss.tools.jst.web.ui+org.eclipse.core.net

flattenDependencies=true
parallelCompilation=true
generateFeatureVersionSuffix=true
individualSourceBundles=true

# don't suppress cleanup if tests fail
noclean=false

# do not sign or pack jars
#skipPack=true
skipSign=true

domainNamespace=*
projNamespace=org.jboss.tools.jmx
projRelengName=org.jboss.tools.jmx.releng

## END PROJECT BUILD PROPERTIES ##
