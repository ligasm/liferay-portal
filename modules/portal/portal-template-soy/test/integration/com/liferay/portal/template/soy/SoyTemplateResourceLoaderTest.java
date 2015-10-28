package com.liferay.portal.template.soy;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateResource;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author Miroslav Ligas
 */
@RunWith(Arquillian.class)
public class SoyTemplateResourceLoaderTest {


	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	private SoyTemplateResourceLoader resourceLoader;

	private String filePath;

	@Before
	public void setUp() throws Exception {
		resourceLoader = new SoyTemplateResourceLoader();

		BundleContext context = FrameworkUtil.
			getBundle(this.getClass()).
			getBundleContext();

		Enumeration<URL> urls =
			context.getBundle().findEntries("com.liferay.portal.template.soy.dependencies", "*", false);

		URL url = urls.nextElement();
		filePath = url.getPath();
	}

	@Test
	public void testGetName() throws Exception {
		String name = resourceLoader.getName();
		Assert.assertEquals(TemplateConstants.LANG_TYPE_SOY, name);
		throw new Exception();
	}

	@Test
	public void testGetTemplateResource() throws Exception {
		TemplateResource templateResource = resourceLoader.getTemplateResource(filePath);

		Assert.assertNotNull(templateResource);

		Reader reader = templateResource.getReader();
		String templateContent = readerToString(reader);

		Assert.assertNotNull(templateContent);
		Assert.assertTrue(templateContent.length() > 0);
	}

	private String readerToString(Reader reader) throws IOException {
		char[] arr = new char[8 * 1024];
		StringBuilder buffer = new StringBuilder();
		int numCharsRead;
		while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
			buffer.append(arr, 0, numCharsRead);
		}
		reader.close();
		return buffer.toString();
	}
}