package org.jboss.tools.archives;

import org.dom4j.io.SAXReader
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import org.dom4j.DocumentHelper

class PackagingConverter {

  def packagingFile
  def projectName
  
  def projectFile
  def projectDir
  def projectDoc
  def projectDescription
  def error = false
  
  PackagingConverter (packagingFile) {
	  this.packagingFile = packagingFile
	  def lastSlashIndex = packagingFile.lastIndexOf('/')
	  if (lastSlashIndex == -1) {
		  lastSlashIndex = packagingFile.lastIndexOf('\\')
		 
	  }
	  if (lastSlashIndex == -1) {
		  println "Error: Path to packaging file is invalid: " + packagingFile
		  error = true
	  }
	  else {
		  projectDir = this.packagingFile.substring(0, lastSlashIndex)
		  projectFile = projectDir + '/.project'
		  
		  def reader = new FileReader(projectFile)
		  projectDoc = new SAXReader().read(reader)
		  projectDescription = projectDoc.rootElement
		  reader.close()
		  
		  projectName = projectDescription.elementText('name')
	  }
  }
  
  def convert () {	  
	  println "Updating Project Builder to 'org.jboss.ide.eclipse.archives.core.archivesBuilder'..."
	  updateBuilder()
	  
	  println "Adding 'org.jboss.ide.eclipse.archives.core.archivesNature' nature..."
	  addNature()
	  
	  println "Backing up to .project.bak, and saving to .project in " + projectDir + "..."
	  saveProject()
	
	  println "Converting JBossIDE 1.x packages to JBoss Tools archives..."
	  convertPackaging()
	  println "Done. Restart Eclipse with JBoss Tools to see changes."
  }
	  
  def updateBuilder () {
	  def buildCommand = projectDescription.element('buildSpec').elements('buildCommand').find {
		  it.elementText('name') == 'org.jboss.ide.eclipse.packaging.core.PackagingBuilder'}
	  
	  if (buildCommand != null) {
		  buildCommand.element('name').setText('org.jboss.ide.eclipse.archives.core.archivesBuilder')
	  }
  }
  
  def addNature () {
	  def natures = projectDescription.element('natures')
	  def nature = natures.addElement('nature')
	  nature.setText('org.jboss.ide.eclipse.archives.core.archivesNature')
  }
  
  def saveProject () {
	  new AntBuilder().copy(file: projectFile, toFile: projectFile+'.bak')
	  saveDoc(projectDoc, projectFile)
  }

  def convertPackaging ()
  {
	  def reader = new FileReader(packagingFile)
	  def doc = new SAXReader().read(reader)
	  def configurations = doc.rootElement
	  reader.close()
	  
	  def packagesDoc = DocumentHelper.createDocument()
	  def packages = packagesDoc.addElement('packages')
	  
	  configurations.elementIterator().each { pkg ->
		  def newPkg = packages.addElement('package')
		  println "Converting package '" + pkg.attributeValue('name') + "'..."
		  
		  newPkg.addAttribute('name', pkg.attributeValue('name'))
		  newPkg.addAttribute('type', 'jar')
		  newPkg.addAttribute('inWorkspace', 'true')
		  newPkg.addAttribute('exploded', pkg.attributeValue('exploded'))
		  
		  if (pkg.attributeValue('destination') == '') {
			  newPkg.addAttribute('todir', '/' + projectName)
		  } else {
			  newPkg.addAttribute('todir', '/' + projectName + '/' + pkg.attributeValue('destination'))
		  }
		  
		  pkg.elementIterator().each { element ->
			  if (element.name == 'file') convertFile(newPkg, element)
			  else convertFolder(newPkg, element)
		  }
	  }
	  
	  savePackaging(packagesDoc)
  }

  def getParent (pkg, prefix) {
	def parent = pkg
	  
  	if (prefix != '')
  	{
	  def folders = prefix.split('/')
	  folders.each { folder ->
	  	def element = parent.addElement('folder')
	  	element.addAttribute('name', folder)
	  	
	  	parent = element
	  }
  	}
	return parent
  }
  
  def convertFile (pkg, file) {
	  def location = file.attributeValue('location')
	  def basedir = location.substring(0, location.lastIndexOf('/'))
	  def filename = location.substring(location.lastIndexOf('/')+1)
	  def parent = getParent(pkg, file.attributeValue('prefix'))
	  def fileset = parent.addElement('fileset')
	  
	  if (file.attribute('projectLocation') != null) {
		fileset.addAttribute('dir', '/' + projectName + '/' + basedir)
		fileset.addAttribute('inWorkspace', 'true')
	  } else {
		fileset.addAttribute('dir', basedir)
		fileset.addAttribute('inWorkspace', 'false')
	  }
	  
	  fileset.addAttribute('includes', filename)
  }
  
  def convertFolder (pkg, folder) {
	  def basedir = folder.attributeValue('location')
	  def parent = getParent(pkg, folder.attributeValue('prefix'))
	  def fileset = parent.addElement('fileset')
	  
	  if (folder.attribute('projectLocation') != null) {
		fileset.addAttribute('dir', '/' + projectName + '/' + basedir)
		fileset.addAttribute('inWorkspace', 'true')
	  } else {
		fileset.addAttribute('dir', basedir)
		fileset.addAttribute('inWorkspace', 'false')
	  }
	  
	  fileset.addAttribute('includes', folder.attributeValue('includes'))
	  fileset.addAttribute('excludes', folder.attributeValue('excludes'))
  }
  
  def savePackaging (packagesDoc) {
	  println "Saving new Archives configuration to '" + projectDir + "/.packages' ..."
	  saveDoc(packagesDoc, new File(projectDir, ".packages"))
  }
  
  def saveDoc (doc, file) {
	  def format = OutputFormat.createPrettyPrint()
	  
	  def fwriter = new FileWriter(file)
	  def writer = new XMLWriter(fwriter, format)
	  
	  writer.write((org.dom4j.Document)doc)
	  fwriter.close()
  }
  
  static void main(args) {
	  if (args.length >= 1)
	  {
	  	def converter = new PackagingConverter(args[0])
	  	if (!converter.error) converter.convert()
	  }
	  else {
		println "Usage: packaging-converter /path/to/.packaging"  
	  }
  }
}