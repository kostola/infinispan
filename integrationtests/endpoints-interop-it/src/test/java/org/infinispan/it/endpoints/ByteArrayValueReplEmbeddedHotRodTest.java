package org.infinispan.it.endpoints;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.VersionedValue;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests embedded and Hot Rod in a replicated clustered environment using byte array values.
 *
 * @author Martin Gencur
 * @author Pedro Ruivo
 * @since 6.0
 */
@Test(groups = "functional", testName = "it.interop.ByteArrayValueReplEmbeddedHotRodTest")
public class ByteArrayValueReplEmbeddedHotRodTest extends AbstractInfinispanTest {

   EndpointsCacheFactory<Object, Object> cacheFactory1;
   EndpointsCacheFactory<Object, Object> cacheFactory2;

   public void testHotRodPutEmbeddedGet() throws Exception {
      final String key = "4";
      final byte[] value = "v1".getBytes();

      // 1. Put with HotRod
      RemoteCache<Object, Object> remote = cacheFactory1.getHotRodCache();
      assertNull(remote.withFlags(Flag.FORCE_RETURN_VALUE).put(key, value));

      // 2. Get with Embedded
      assertArrayEquals(value, (byte[]) cacheFactory2.getEmbeddedCache().get(key));
   }

   public void testHotRodReplace() throws Exception {
      final String key = "5";
      final byte[] value1 = "v1".getBytes();
      final byte[] value2 = "v2".getBytes();

      // 1. Put with HotRod
      RemoteCache<Object, Object> remote = cacheFactory1.getHotRodCache();
      assertNull(remote.withFlags(Flag.FORCE_RETURN_VALUE).put(key, value1));

      // 2. Replace with HotRod
      VersionedValue versioned = cacheFactory1.getHotRodCache().getWithMetadata(key);
      assertTrue(cacheFactory1.getHotRodCache().replaceWithVersion(key, value2, versioned.getVersion()));
   }

   public void testHotRodRemove() throws Exception {
      final String key = "7";
      final byte[] value = "v1".getBytes();

      // 1. Put with HotRod
      RemoteCache<Object, Object> remote = cacheFactory1.getHotRodCache();
      assertNull(remote.withFlags(Flag.FORCE_RETURN_VALUE).put(key, value));

      // 2. Remove with HotRod
      VersionedValue versioned = cacheFactory1.getHotRodCache().getWithMetadata(key);
      assertTrue(cacheFactory1.getHotRodCache().removeWithVersion(key, versioned.getVersion()));
   }

   //This test can fail only if there's a marshaller specified for EmbeddedTypeConverter
   public void testEmbeddedPutHotRodGet() throws Exception {
      final String key = "8";
      final byte[] value = "v1".getBytes();

      // 1. Put with Embedded
      assertNull(cacheFactory2.getEmbeddedCache().put(key, value));

      // 2. Get with HotRod
      assertArrayEquals(value, (byte[]) cacheFactory1.getHotRodCache().get(key));
   }

   @BeforeClass
   protected void setup() throws Exception {
      cacheFactory1 = new EndpointsCacheFactory<>(CacheMode.REPL_SYNC).setup();
      cacheFactory2 = new EndpointsCacheFactory<>(CacheMode.REPL_SYNC).setup();
   }

   @AfterClass
   protected void teardown() {
      EndpointsCacheFactory.killCacheFactories(cacheFactory1, cacheFactory2);
   }
}
