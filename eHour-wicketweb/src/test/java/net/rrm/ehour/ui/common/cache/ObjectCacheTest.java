/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.ui.common.cache;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
/**
 * TODO 
 **/

public class ObjectCacheTest
{

	@Test
	public void testAddObjectToCache()
	{
		ObjectCache reportCache = new ObjectCache();

		MockReport report = new MockReport();
		long id = report.id;
		String cacheId = reportCache.addObjectToCache(report);

		report = null;
		
		report = (MockReport)reportCache.getObjectFromCache(cacheId);
		
		assertEquals(id, report.id);
	}
	
	class MockReport implements CachableObject
	{
		private static final long serialVersionUID = 1L;
		long id = new Date().getTime();
		private String cacheId;
		/**
		 * @return the cacheId
		 */
		public String getCacheId()
		{
			return cacheId;
		}
		/**
		 * @param cacheId the cacheId to set
		 */
		public void setCacheId(String cacheId)
		{
			this.cacheId = cacheId;
		}

		
	}
}
