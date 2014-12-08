package config;

import java.util.Arrays;
import java.util.List;



public class GlobalConfig {
	public static String SVNURL = "file:///z:/"; 
	
	public static String mainDir = "c:/Research/Fall2013/working/";
	public static String mainDummyDir = mainDir + "dummy_dir/";
	public static String slotDummyDir = mainDummyDir + "slot0/";
	public static String dummyDir = slotDummyDir + "0/";
//	public static String dummyDir = "f:/dummy_dir/";
	public static String slotMapPath = mainDir + "slotMap.dat";
	public static String hashDBDir = mainDir + "hash_database/"; 
	public static String hashDBPath = mainDir + "hash_database.dat"; 

	public static String projectDataDir = mainDir + "project_data/";
	public static String localRepoDir = "f:/Repositories/";
//	public static String processJavaDirPath = "data/db4o/java_src/";
	public static String processJavaDirPath = mainDir + "data/db4o/java_src/";
	
	
	public static String[] sourceFileJavaExt = {".java"};
	
	public static String name = "anh";
	public static String password = "Vietus09";
	

	public static boolean isReadDirectFromRepo = false;
	
	
	public static String repoFilePath = mainDir + "repoList.txt";
	
	public static String intermediateData = mainDir + "intermediate_data/";
	public static String dataExt = ".dat";

	public static String graphDBPath = intermediateData+"graphDB" + dataExt;
	
	/*
	 * Limit maximum size of Groum. Important due to it affect storage capacity and recommend quality
	 */
	public static int maxGroumSize = 10;//Integer.MAX_VALUE;//10000;
	
	
	public static int topListSize = 30;
	public static boolean isFilterLirary = true;
//	public static String[] concernedLibs = {"java.util.", "java.lang.", "java.io"}; 
	
	/**
	 * For JDK
	 */
	public static String[] concernedLibs = 
		{
		"ControlInfo[",
		"java.applet", "java.awt", "java.awt.color", "java.awt.datatransfer", "java.awt.dnd", "java.awt.event", "java.awt.font", 
		"java.awt.geom", "java.awt.im", "java.awt.im.spi", "java.awt.image", "java.awt.image.renderable", "java.awt.print", 
		"java.beans", "java.beans.beancontext", "java.io", "java.lang", "java.lang.annotation", "java.lang.instrument", 
		"java.lang.invoke", "java.lang.management", "java.lang.ref", "java.lang.reflect", "java.math", "java.net", 
		"java.nio", "java.nio.channels", "java.nio.channels.spi", "java.nio.charset", "java.nio.charset.spi", "java.nio.file", 
		"java.nio.file.attribute", "java.nio.file.spi", "java.rmi", "java.rmi.activation", "java.rmi.dgc", "java.rmi.registry", 
		"java.rmi.server", "java.security", "java.security.acl", "java.security.cert", "java.security.interfaces", "java.security.spec", 
		"java.sql", "java.text", "java.text.spi", "java.util", "java.util.concurrent", "java.util.concurrent.atomic", 
		"java.util.concurrent.locks", "java.util.jar", "java.util.logging", "java.util.prefs", "java.util.regex", "java.util.spi", 
		"java.util.zip", "javax.accessibility", "javax.activation", "javax.activity", "javax.annotation", "javax.annotation.processing", 
		"javax.crypto", "javax.crypto.interfaces", "javax.crypto.spec", "javax.imageio", "javax.imageio.event", "javax.imageio.metadata", 
		"javax.imageio.plugins.bmp", "javax.imageio.plugins.jpeg", "javax.imageio.spi", "javax.imageio.stream", "javax.jws", 
		"javax.jws.soap", "javax.lang.model", "javax.lang.model.element", "javax.lang.model.type", "javax.lang.model.util", 
		"javax.management", "javax.management.loading", "javax.management.modelmbean", "javax.management.monitor", 
		"javax.management.openmbean", "javax.management.relation", "javax.management.remote", "javax.management.remote.rmi", 
		"javax.management.timer", "javax.naming", "javax.naming.directory", "javax.naming.event", "javax.naming.ldap", 
		"javax.naming.spi", "javax.net", "javax.net.ssl", "javax.print", "javax.print.attribute", "javax.print.attribute.standard", 
		"javax.print.event", "javax.rmi", "javax.rmi.CORBA", "javax.rmi.ssl", "javax.script", "javax.security.auth", 
		"javax.security.auth.callback", "javax.security.auth.kerberos", "javax.security.auth.login", "javax.security.auth.spi", 
		"javax.security.auth.x500", "javax.security.cert", "javax.security.sasl", "javax.sound.midi", "javax.sound.midi.spi", 
		"javax.sound.sampled", "javax.sound.sampled.spi", "javax.sql", "javax.sql.rowset", "javax.sql.rowset.serial", 
		"javax.sql.rowset.spi", "javax.swing", "javax.swing.border", "javax.swing.colorchooser", "javax.swing.event", 
		"javax.swing.filechooser", "javax.swing.plaf", "javax.swing.plaf.basic", "javax.swing.plaf.metal", "javax.swing.plaf.multi", 
		"javax.swing.plaf.nimbus", "javax.swing.plaf.synth", "javax.swing.table", "javax.swing.text", "javax.swing.text.html", 
		"javax.swing.text.html.parser", "javax.swing.text.rtf", "javax.swing.tree", "javax.swing.undo", "javax.tools", 
		"javax.transaction", "javax.transaction.xa", "javax.xml", "javax.xml.bind", "javax.xml.bind.annotation", 
		"javax.xml.bind.annotation.adapters", "javax.xml.bind.attachment", "javax.xml.bind.helpers", "javax.xml.bind.util", 
		"javax.xml.crypto", "javax.xml.crypto.dom", "javax.xml.crypto.dsig", "javax.xml.crypto.dsig.dom", "javax.xml.crypto.dsig.keyinfo", 
		"javax.xml.crypto.dsig.spec", "javax.xml.datatype", "javax.xml.namespace", "javax.xml.parsers", "javax.xml.soap", 
		"javax.xml.stream", "javax.xml.stream.events", "javax.xml.stream.util", "javax.xml.transform", "javax.xml.transform.dom",
		"javax.xml.transform.sax", "javax.xml.transform.stax", "javax.xml.transform.stream", "javax.xml.validation", "javax.xml.ws",
		"javax.xml.ws.handler", "javax.xml.ws.handler.soap", "javax.xml.ws.http", "javax.xml.ws.soap", "javax.xml.ws.spi", 
		"javax.xml.ws.spi.http", "javax.xml.ws.wsaddressing", "javax.xml.xpath", "org.ietf.jgss", "org.omg.CORBA", "org.omg.CORBA_2_3", 
		"org.omg.CORBA_2_3.portable", "org.omg.CORBA.DynAnyPackage", "org.omg.CORBA.ORBPackage", "org.omg.CORBA.portable",
		"org.omg.CORBA.TypeCodePackage", "org.omg.CosNaming", "org.omg.CosNaming.NamingContextExtPackage", 
		"org.omg.CosNaming.NamingContextPackage", "org.omg.Dynamic", "org.omg.DynamicAny", "org.omg.DynamicAny.DynAnyFactoryPackage", 
		"org.omg.DynamicAny.DynAnyPackage", "org.omg.IOP", "org.omg.IOP.CodecFactoryPackage", "org.omg.IOP.CodecPackage", 
		"org.omg.Messaging", "org.omg.PortableInterceptor", "org.omg.PortableInterceptor.ORBInitInfoPackage", "org.omg.PortableServer", 
		"org.omg.PortableServer.CurrentPackage", "org.omg.PortableServer.POAManagerPackage", "org.omg.PortableServer.POAPackage", 
		"org.omg.PortableServer.portable", "org.omg.PortableServer.ServantLocatorPackage", "org.omg.SendingContext", 
		"org.omg.stub.java.rmi", "org.w3c.dom", "org.w3c.dom.bootstrap", "org.w3c.dom.events", "org.w3c.dom.ls", "org.xml.sax", 
		"org.xml.sax.ext", "org.xml.sax.helpers"};
	
		public static String[] notConcernedLibs = {};
	
	public static String[] containedLibs = 
	{
	"java.applet", "java.awt", "java.awt.color", "java.awt.datatransfer", "java.awt.dnd", "java.awt.event", "java.awt.font", 
	"java.awt.geom", "java.awt.im", "java.awt.im.spi", "java.awt.image", "java.awt.image.renderable", "java.awt.print", 
	"java.beans", "java.beans.beancontext", "java.io", "java.lang", "java.lang.annotation", "java.lang.instrument", 
	"java.lang.invoke", "java.lang.management", "java.lang.ref", "java.lang.reflect", "java.math", "java.net", 
	"java.nio", "java.nio.channels", "java.nio.channels.spi", "java.nio.charset", "java.nio.charset.spi", "java.nio.file", 
	"java.nio.file.attribute", "java.nio.file.spi", "java.rmi", "java.rmi.activation", "java.rmi.dgc", "java.rmi.registry", 
	"java.rmi.server", "java.security", "java.security.acl", "java.security.cert", "java.security.interfaces", "java.security.spec", 
	"java.sql", "java.text", "java.text.spi", "java.util", "java.util.concurrent", "java.util.concurrent.atomic", 
	"java.util.concurrent.locks", "java.util.jar", "java.util.logging", "java.util.prefs", "java.util.regex", "java.util.spi", 
	"java.util.zip", "javax.accessibility", "javax.activation", "javax.activity", "javax.annotation", "javax.annotation.processing", 
	"javax.crypto", "javax.crypto.interfaces", "javax.crypto.spec", "javax.imageio", "javax.imageio.event", "javax.imageio.metadata", 
	"javax.imageio.plugins.bmp", "javax.imageio.plugins.jpeg", "javax.imageio.spi", "javax.imageio.stream", "javax.jws", 
	"javax.jws.soap", "javax.lang.model", "javax.lang.model.element", "javax.lang.model.type", "javax.lang.model.util", 
	"javax.management", "javax.management.loading", "javax.management.modelmbean", "javax.management.monitor", 
	"javax.management.openmbean", "javax.management.relation", "javax.management.remote", "javax.management.remote.rmi", 
	"javax.management.timer", "javax.naming", "javax.naming.directory", "javax.naming.event", "javax.naming.ldap", 
	"javax.naming.spi", "javax.net", "javax.net.ssl", "javax.print", "javax.print.attribute", "javax.print.attribute.standard", 
	"javax.print.event", "javax.rmi", "javax.rmi.CORBA", "javax.rmi.ssl", "javax.script", "javax.security.auth", 
	"javax.security.auth.callback", "javax.security.auth.kerberos", "javax.security.auth.login", "javax.security.auth.spi", 
	"javax.security.auth.x500", "javax.security.cert", "javax.security.sasl", "javax.sound.midi", "javax.sound.midi.spi", 
	"javax.sound.sampled", "javax.sound.sampled.spi", "javax.sql", "javax.sql.rowset", "javax.sql.rowset.serial", 
	"javax.sql.rowset.spi", "javax.swing", "javax.swing.border", "javax.swing.colorchooser", "javax.swing.event", 
	"javax.swing.filechooser", "javax.swing.plaf", "javax.swing.plaf.basic", "javax.swing.plaf.metal", "javax.swing.plaf.multi", 
	"javax.swing.plaf.nimbus", "javax.swing.plaf.synth", "javax.swing.table", "javax.swing.text", "javax.swing.text.html", 
	"javax.swing.text.html.parser", "javax.swing.text.rtf", "javax.swing.tree", "javax.swing.undo", "javax.tools", 
	"javax.transaction", "javax.transaction.xa", "javax.xml", "javax.xml.bind", "javax.xml.bind.annotation", 
	"javax.xml.bind.annotation.adapters", "javax.xml.bind.attachment", "javax.xml.bind.helpers", "javax.xml.bind.util", 
	"javax.xml.crypto", "javax.xml.crypto.dom", "javax.xml.crypto.dsig", "javax.xml.crypto.dsig.dom", "javax.xml.crypto.dsig.keyinfo", 
	"javax.xml.crypto.dsig.spec", "javax.xml.datatype", "javax.xml.namespace", "javax.xml.parsers", "javax.xml.soap", 
	"javax.xml.stream", "javax.xml.stream.events", "javax.xml.stream.util", "javax.xml.transform", "javax.xml.transform.dom",
	"javax.xml.transform.sax", "javax.xml.transform.stax", "javax.xml.transform.stream", "javax.xml.validation", "javax.xml.ws",
	"javax.xml.ws.handler", "javax.xml.ws.handler.soap", "javax.xml.ws.http", "javax.xml.ws.soap", "javax.xml.ws.spi", 
	"javax.xml.ws.spi.http", "javax.xml.ws.wsaddressing", "javax.xml.xpath", "org.ietf.jgss", "org.omg.CORBA", "org.omg.CORBA_2_3", 
	"org.omg.CORBA_2_3.portable", "org.omg.CORBA.DynAnyPackage", "org.omg.CORBA.ORBPackage", "org.omg.CORBA.portable",
	"org.omg.CORBA.TypeCodePackage", "org.omg.CosNaming", "org.omg.CosNaming.NamingContextExtPackage", 
	"org.omg.CosNaming.NamingContextPackage", "org.omg.Dynamic", "org.omg.DynamicAny", "org.omg.DynamicAny.DynAnyFactoryPackage", 
	"org.omg.DynamicAny.DynAnyPackage", "org.omg.IOP", "org.omg.IOP.CodecFactoryPackage", "org.omg.IOP.CodecPackage", 
	"org.omg.Messaging", "org.omg.PortableInterceptor", "org.omg.PortableInterceptor.ORBInitInfoPackage", "org.omg.PortableServer", 
	"org.omg.PortableServer.CurrentPackage", "org.omg.PortableServer.POAManagerPackage", "org.omg.PortableServer.POAPackage", 
	"org.omg.PortableServer.portable", "org.omg.PortableServer.ServantLocatorPackage", "org.omg.SendingContext", 
	"org.omg.stub.java.rmi", "org.w3c.dom", "org.w3c.dom.bootstrap", "org.w3c.dom.events", "org.w3c.dom.ls", "org.xml.sax", 
	"org.xml.sax.ext", "org.xml.sax.helpers"};
	
	
	
	
//	/**
//	 * For java.util
//	 */
//	public static String[] concernedLibs = 
//		{
//		"ControlInfo[",
//		"java.util"};
//	
////	public static String[] notConcernedLibs = 
////		{
////		"java.util.concurrent", "java.util.concurrent.atomic", 
////		"java.util.concurrent.locks", "java.util.jar", "java.util.logging", "java.util.prefs", "java.util.regex", "java.util.spi", 
////		"java.util.zip"};
////	
//	public static String[] notConcernedLibs = 
//		{};
//	
//	public static String[] containedLibs = 
//		{
//		"java.util"};
	
	public static int slotDiv = 1000;
	
	public static int dataDiv = 100000;

	public static int numLevel1Hash = 100000;
	
	
	public static int minCachedItemSize = 10000000;//Integer.MAX_VALUE;//50000000;//Integer.MAX_VALUE;
	public static int maxLowCachedItemSize =10000000;//Integer.MAX_VALUE;//100000;//Integer.MAX_VALUE;

	
	public static int SVNWindowSize = 100;
	
	public static boolean useTrunkOnly = false;
	
	public static boolean useSourcePathFilter = true;

	public static boolean isRedirectErrMsg = true;
	public static String logPath = mainDir + "logging/err_log.txt"; 
	public static String logGroumGenPath = mainDir + "logging/err_log_groum_gen.txt"; 

	public static int maxUnCompSize = 1500000000;
	
	public static boolean isStartFromBeginning = false;//true;
	
	public static int startMergeSlotCount = 141;
	public static int endMergeSlotCount = 200;

	public static int backupStep = 20;
	
	public static Integer[] cachedLevel1Vals = {
//			10172,
//			11025,
//			2762,
//			3922,
//			12407,
//			8459,
//			4696,
//			12933,
//			2571,
//			12226,
//			1967,
//			12924
			};
	
	
	
	public static List<Integer> cacheLevel1ValList = Arrays.asList(cachedLevel1Vals); 
	
	
	
	public static void refreshParams(){
		mainDummyDir = mainDir + "dummy_dir/";
		slotDummyDir = mainDummyDir + "slot0/";
		dummyDir = slotDummyDir + "0/";
		slotMapPath = mainDir + "slotMap.dat";
		hashDBDir = mainDir + "hash_database/"; 
		hashDBPath = mainDir + "hash_database.dat"; 

		projectDataDir = mainDir + "project_data/";
		processJavaDirPath = mainDir + "data/db4o/java_src/";
		
		repoFilePath = mainDir + "repoList.txt";
		intermediateData = mainDir + "intermediate_data/";
		graphDBPath = intermediateData+"graphDB" + dataExt;
		logPath = mainDir + "logging/"; 
		logGroumGenPath = mainDir + "logging/err_log_groum_gen.txt";
	}
}
