/**
 * 
 */
package org.rifidi.edge.core.junit;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rifidi.edge.core.readerPlugin.ISpecificReaderPluginFactory;
import org.rifidi.edge.core.readerPluginService.ReaderPluginRegistryService;
import org.rifidi.edge.core.readerPluginService.ReaderPluginRegistryServiceImpl;
import org.rifidi.edge.readerplugin.dummy.DummyReaderInfo;
import org.rifidi.edge.readerplugin.dummy.DummyReaderPluginFactory;


/**
 * @author Matthew Dean - matt@pramari.com
 *
 */
public class ReaderAdapterRegistryTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test to see if we can register an adapter in the readerAdapterFactory and get it back
	 */
	@Test
	public void testRegisterReaderAdapter() {

		ReaderPluginRegistryService registry = new ReaderPluginRegistryServiceImpl();
		
		ISpecificReaderPluginFactory factory = new DummyReaderPluginFactory();
		registry.registerReaderAdapter(DummyReaderInfo.class, factory);
		
		if (registry.getSpecReaderAdapterFactory(new DummyReaderInfo()) != factory)
		{
			Assert.fail();
		}
		
		/*registry.getSpecReaderAdapterFactory() */
	
	}
	
	/**
	 * Tests connecting the reader
	 */
	@Test
	public void testRegisterListener() {
		//TODO look at this later
	}
	
	/**
	 * Tests connecting the reader
	 */
	@Test
	public void testUnRegisterListener() {
		//TODO look at this later
		Assert.fail();

	}
}
