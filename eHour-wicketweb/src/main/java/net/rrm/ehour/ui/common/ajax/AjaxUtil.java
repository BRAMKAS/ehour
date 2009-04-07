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

package net.rrm.ehour.ui.common.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Component.IVisitor;

/**
 * 
 * @author Thies
 *
 */
public class AjaxUtil
{
	/**
	 * Publish ajax event to container and upwards upto and including the page
	 * @param ajaxEvent
	 * @param container
	 */
	public static void publishAjaxEvent(MarkupContainer container, AjaxEvent ajaxEvent)
	{
		boolean proceed = true;
		
		if (container instanceof AjaxAwareContainer)
		{
			proceed = ((AjaxAwareContainer)container).ajaxEventReceived(ajaxEvent);
		}
		
		if (proceed && !(container instanceof Page))
		{
			MarkupContainer parent = container.getParent();
			
			if (parent == null)
			{
				parent = container.getPage();
			}
			
			publishAjaxEvent(parent, ajaxEvent);
		}
	}
	
	/**
	 * Publish ajax event to page itself and then all it's children
	 * @param parent
	 * @param event
	 */
	public static void publishAjaxEventToPageChildren(MarkupContainer parent, AjaxEvent event)
	{
		IVisitor visitor = new AjaxEventVisitor(event);
		
		visitor.component(parent.getPage());
		parent.getPage().visitChildren(AjaxAwareContainer.class, visitor);
	}
	
	/**
	 * Publish ajax event to children of parent container
	 * @param parent
	 * @param event
	 */
	public static void publishAjaxEventToParentChildren(MarkupContainer parent, AjaxEvent event)
	{
		parent.visitChildren(AjaxAwareContainer.class, new AjaxEventVisitor(event));
	}	
	
	/**
	 * 
	 * @author Thies
	 *
	 */
	private static class AjaxEventVisitor implements IVisitor
	{
		private AjaxEvent event;
		
		private AjaxEventVisitor(AjaxEvent ajaxEvent)
		{
			this.event = ajaxEvent;
		}

		public Object component(Component component)
		{
			boolean proceed = true;
			
			if (component instanceof AjaxAwareContainer)
			{
				proceed = ((AjaxAwareContainer)component).ajaxEventReceived(event);
			}
			
			return proceed ? IVisitor.CONTINUE_TRAVERSAL : IVisitor.STOP_TRAVERSAL;
		}
		
	}	
}
