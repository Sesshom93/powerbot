package org.powerbot.game.api.methods.node;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.util.node.Deque;
import org.powerbot.game.api.util.node.Queue;
import org.powerbot.game.bot.Bot;
import org.powerbot.game.client.Client;
import org.powerbot.game.client.MenuGroupNode;
import org.powerbot.game.client.MenuGroupNodeItems;
import org.powerbot.game.client.MenuItemNode;
import org.powerbot.game.client.MenuItemNodeAction;
import org.powerbot.game.client.MenuItemNodeOption;

/**
 * @author Timer
 */
public class Menu {
	private static final Pattern HTML_TAG = Pattern.compile("(^[^<]+>|<[^>]+>|<[^>]+$)");

	/**
	 * @return The position of the latest opened menu.
	 */
	public static Point getLocation() {
		final Bot bot = Bot.resolve();
		return new Point(
				bot.getClient().getMenuX() * bot.multipliers.GLOBAL_MENUX,
				bot.getClient().getMenuY() * bot.multipliers.GLOBAL_MENUY
		);
	}

	/**
	 * @return The position of the opened sub (collapsed) menu.
	 */
	public static Point getSubLocation() {
		final Bot bot = Bot.resolve();
		return new Point(
				bot.getClient().getSubMenuX() * bot.multipliers.GLOBAL_SUBMENUX,
				bot.getClient().getSubMenuY() * bot.multipliers.GLOBAL_SUBMENUY
		);
	}

	/**
	 * @return The width of the opened menu.
	 */
	public static int getWidth() {
		final Bot bot = Bot.resolve();
		return bot.getClient().getMenuWidth() * bot.multipliers.GLOBAL_MENUWIDTH;
	}

	/**
	 * @return The height of the opened menu.
	 */
	public static int getHeight() {
		final Bot bot = Bot.resolve();
		return bot.getClient().getMenuHeight() * bot.multipliers.GLOBAL_MENUHEIGHT;
	}

	/**
	 * @return The width of the opened sub (collapsed) menu.
	 */
	public static int getSubWidth() {
		final Bot bot = Bot.resolve();
		return bot.getClient().getSubMenuWidth() * bot.multipliers.GLOBAL_SUBMENUWIDTH;
	}

	public static boolean isOpen() {
		return Bot.resolve().getClient().isMenuOpen();
	}

	public static boolean isCollapsed() {
		return Bot.resolve().getClient().isMenuCollapsed();
	}

	public static boolean select(final String action) {
		return select(action, null);
	}

	public static boolean select(final String action, final String option) {
		int idx = getIndex(action, option);
		if (!isOpen()) {
			if (idx == -1) {
				return false;
			}
			if (idx == 0) {
				Mouse.click(true);
				return true;
			}
			Mouse.click(false);
			final Timer t = new Timer(100);
			while (t.isRunning() && !isOpen()) {
				Time.sleep(5);
			}
			idx = getIndex(action, option);

			return idx != -1 && clickIndex(idx);
		} else if (idx == -1) {
			while (isOpen()) {
				Mouse.move(0, 0);
				Time.sleep(Random.nextInt(100, 500));
			}
			return false;
		}
		return clickIndex(idx);
	}

	private static boolean clickIndex(final int i) {
		if (!isOpen()) {
			return false;
		}
		final String[] items = getItems();
		if (items.length <= i) {
			return false;
		}
		if (isCollapsed()) {
			final Queue<MenuGroupNode> groups = new Queue<MenuGroupNode>(Bot.resolve().getClient().getCollapsedMenuItems());
			int idx = 0, mainIdx = 0;
			for (MenuGroupNode g = groups.getHead(); g != null; g = groups.getNext(), ++mainIdx) {
				final Queue<MenuItemNode> subItems = new Queue<MenuItemNode>(((MenuGroupNodeItems) g.getData()).getMenuGroupNodeItems());
				int subIdx = 0;
				for (MenuItemNode item = subItems.getHead(); item != null; item = subItems.getNext(), ++subIdx) {
					if (idx++ == i) {
						return subIdx == 0 ? clickMain(items, mainIdx) : clickSub(items, mainIdx, subIdx);
					}
				}
			}
			return false;
		} else {
			return clickMain(items, i);
		}
	}

	public static String[] getItems() {
		final String[] options = getOptions();
		final String[] actions = getActions();

		final LinkedList<String> output = new LinkedList<String>();

		final int len = Math.min(options.length, actions.length);
		for (int i = 0; i < len; i++) {
			final String option = options[i];
			final String action = actions[i];
			if (option != null && action != null) {
				final String text = action + " " + option;
				output.add(text.trim());
			}
		}

		return output.toArray(new String[output.size()]);
	}

	public static boolean contains(final String action) {
		return getIndex(action) != -1;
	}

	public static boolean contains(final String action, final String option) {
		return getIndex(action, option) != -1;
	}

	private static boolean clickMain(final String[] items, final int i) {
		final Point menu = getLocation();
		final int xOff = Random.nextInt(4, items[i].length() * 4);
		final int yOff = 21 + 16 * i + Random.nextInt(3, 12);
		Mouse.move(menu.x + xOff, menu.y + yOff, 2, 2);
		if (isOpen()) {
			Mouse.click(true);
			return true;
		}
		return false;
	}

	private static boolean clickSub(final String[] items, final int mIdx, final int sIdx) {
		final Point menuLoc = getLocation();
		int x = Random.nextInt(4, items[mIdx].length() * 4);
		int y = 21 + 16 * mIdx + Random.nextInt(3, 12);
		Mouse.move(menuLoc.x + x, menuLoc.y + y, 2, 2);
		Time.sleep(Random.nextInt(125, 150));
		if (isOpen()) {
			final Point subLoc = getSubLocation();
			final Point start = Mouse.getLocation();
			final int subOff = subLoc.x - start.x;
			x = Random.nextInt(4, items[sIdx].length() * 4);
			if (subOff > 0) {
				Mouse.move(Random.nextInt(subOff + 4, subOff + Random.nextInt(4, items[sIdx].length() * 2)), start.y);
			} else {
				Mouse.move(subLoc.x + x, Mouse.getLocation().y, 2, 0);
			}
			Time.sleep(Random.nextInt(125, 150));
			if (isOpen()) {
				y = 16 * sIdx + Random.nextInt(3, 12) + 21;
				Mouse.move(subLoc.x + x, subLoc.y + y, 0, 2);
				Time.sleep(Random.nextInt(125, 150));
				if (isOpen()) {
					Mouse.click(true);
					return true;
				}
			}
		}
		return false;
	}

	public static String[] getActions() {
		return getMenuItemPart(true);
	}

	public static String[] getOptions() {
		return getMenuItemPart(false);
	}

	private static int getIndex(String action) {
		action = action.toLowerCase();
		final String[] items = getActions();
		for (int i = 0; i < items.length; i++) {
			if (items[i].toLowerCase().contains(action)) {
				return i;
			}
		}
		return -1;
	}

	private static int getIndex(String action, String option) {
		if (option == null) {
			return getIndex(action);
		}
		action = action.toLowerCase();
		option = option.toLowerCase();
		final String[] actions = getActions();
		final String[] options = getOptions();
		for (int i = 0; i < Math.min(actions.length, options.length); i++) {
			if (actions[i].toLowerCase().contains(action) && options[i].toLowerCase().contains(option)) {
				return i;
			}
		}
		return -1;
	}

	private static String[] getMenuItemPart(final boolean firstPart) {
		final LinkedList<String> itemsList = new LinkedList<String>();
		final Client client = Bot.resolve().getClient();
		if (isCollapsed()) {
			final Queue<MenuGroupNode> menu = new Queue<MenuGroupNode>(client.getCollapsedMenuItems());
			try {
				for (MenuGroupNode mgn = menu.getHead(); mgn != null; mgn = menu.getNext()) {
					final Queue<MenuItemNode> submenu = new Queue<MenuItemNode>(((MenuGroupNodeItems) mgn.getData()).getMenuGroupNodeItems());
					for (MenuItemNode min = submenu.getHead(); min != null; min = submenu.getNext()) {
						itemsList.addLast(firstPart ?
								(String) ((MenuItemNodeAction) min.getData()).getMenuItemNodeAction() :
								(String) ((MenuItemNodeOption) min.getData()).getMenuItemNodeOption());
					}
				}
			} catch (final NullPointerException ignored) {
			}
		} else {
			try {
				final Deque<MenuItemNode> menu = new Deque<MenuItemNode>(client.getMenuItems());
				for (MenuItemNode min = menu.getHead(); min != null; min = menu.getNext()) {
					itemsList.addLast(firstPart ?
							(String) ((MenuItemNodeAction) min.getData()).getMenuItemNodeAction() :
							(String) ((MenuItemNodeOption) min.getData()).getMenuItemNodeOption());
				}
			} catch (final Throwable ignored) {
			}
		}
		final String[] items = itemsList.toArray(new String[itemsList.size()]);
		final LinkedList<String> output = new LinkedList<String>();
		for (int i = items.length - 1; i >= 0; i--) {
			final String item = items[i];
			output.add(item == null ? "" : stripFormatting(item));
		}
		if (output.size() > 1 && (isCollapsed() ? output.getLast() : output.getFirst()).equals(firstPart ? "Cancel" : "")) {
			Collections.reverse(output);
		}
		return output.toArray(new String[output.size()]);
	}

	private static String stripFormatting(final String input) {
		return HTML_TAG.matcher(input).replaceAll("");
	}
}
