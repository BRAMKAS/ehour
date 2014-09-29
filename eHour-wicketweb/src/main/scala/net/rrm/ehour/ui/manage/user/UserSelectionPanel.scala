package net.rrm.ehour.ui.manage.user

import java.util
import java.util.Collections

import net.rrm.ehour.domain.User
import net.rrm.ehour.sort.UserComparator
import net.rrm.ehour.ui.common.border.GreyRoundedBorder
import net.rrm.ehour.ui.common.panel.AbstractBasePanel
import net.rrm.ehour.ui.common.panel.entryselector.EntrySelectorPanel.ITEM_LIST_HOLDER_ID
import net.rrm.ehour.ui.common.panel.entryselector._
import net.rrm.ehour.ui.common.wicket.Event
import net.rrm.ehour.user.service.UserService
import org.apache.wicket.AttributeModifier
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.event.{Broadcast, IEvent}
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.panel.Fragment
import org.apache.wicket.model.{IModel, ResourceModel}
import org.apache.wicket.spring.injection.annot.SpringBean

class UserSelectionPanel(id: String, titleResourceKey: Option[String], filterUsers: (util.List[User]) => util.List[User]) extends AbstractBasePanel[LdapUserBackingBean](id) {
  def this(id: String, titleResourceKey: Option[String]) = this(id, titleResourceKey, xs => xs)

  val Self = this

  val hideInactiveFilter = new HideInactiveFilter()

  var container: Fragment = _

  @SpringBean
  protected var userService: UserService = _

  override def onInitialize() {
    super.onInitialize()

    val greyBorder = titleResourceKey match {
      case Some(resourceKey) => new GreyRoundedBorder("border", new ResourceModel(resourceKey))
      case None => new GreyRoundedBorder("border")
    }

    addOrReplace(greyBorder)

    container = createListView()
    val selectorPanel = new EntrySelectorPanel("entrySelectorFrame", container, new ResourceModel("admin.user.hideInactive"))

    greyBorder.add(selectorPanel)
  }

  def createListView() = {
    val userListView = new EntrySelectorListView[User]("itemList", users) {
      protected def onPopulate(item: ListItem[User], itemModel: IModel[User]) {
        val user = item.getModelObject

        if (!user.isActive) {
          item.add(AttributeModifier.append("class", "inactive"))
        }

        item.add(new Label("name", user.getName))
        item.add(new Label("userName", user.getUsername))
      }

      protected def onClick(item: ListItem[User], target: AjaxRequestTarget) {
        val userId = item.getModelObject.getUserId

        send(Self.getPage, Broadcast.BREADTH, EntrySelectedEvent(userId, target))
      }
    }

    val fragment = new Fragment(ITEM_LIST_HOLDER_ID, "userSelection", UserSelectionPanel.this)
    fragment.add(userListView)
    fragment.setOutputMarkupId(true)
    fragment
  }

  override def onEvent(event: IEvent[_]) {
    def refresh(event: Event) {
      val component = container.get("itemList")
      component.asInstanceOf[EntrySelectorListView[User]].setList(users)

      event.refresh(container)
    }

    event.getPayload match {
      case event: EntryListUpdatedEvent => refresh(event)
      case event: InactiveFilterChangedEvent => {
        hideInactiveFilter.setHideInactive(event.hideInactiveFilter.isHideInactive)
        refresh(event)
      }
      case _ =>
    }
  }

  private def users: util.List[User] = {
    val users: util.List[User] = filterUsers(if (hideInactiveFilter.isHideInactive) userService.getUsers() else userService.getUsers)
    Collections.sort(users, new UserComparator())
    users
  }
}
