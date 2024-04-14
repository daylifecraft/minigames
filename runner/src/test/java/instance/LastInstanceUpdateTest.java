package instance;

import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.instance.AbstractCraftInstance;
import com.daylifecraft.minigames.instance.CraftInstancesManager;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.UUID;

class LastInstanceUpdateTest {

  private static final CraftInstancesManager craftInstancesManager =
    Mockito.spy(new CraftInstancesManager(Mockito.mock(InstanceManager.class)));

  private static final Instance spawnInstance = Mockito.mock(Instance.class);
  private static final AbstractCraftInstance abstractCraftInstance =
    Mockito.mock(AbstractCraftInstance.class);

  private static Player player;

  private static final UUID playerUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

  @BeforeAll
  static void setup() {
    Init.enableTests();
    Init.setupCraftInstancesManager(craftInstancesManager);

    player = Mockito.mock(Player.class);

    Mockito.doReturn(playerUuid).when(player).getUuid();

    Mockito.doReturn(abstractCraftInstance)
      .when(craftInstancesManager)
      .getInstance(ArgumentMatchers.any());
    Mockito.doCallRealMethod().when(craftInstancesManager).notifyPlayerSpawn(spawnInstance, player);
    Mockito.doCallRealMethod()
      .when(craftInstancesManager)
      .notifyPlayerDisconnect(spawnInstance, player);

    Mockito.doCallRealMethod().when(craftInstancesManager).getLastPlayerInstance(player);
  }

  @Test
  void testDoesInstanceUpdatedOnSpawn() {
    craftInstancesManager.notifyPlayerSpawn(spawnInstance, player);

    Assertions.assertEquals(
      spawnInstance,
      craftInstancesManager.getLastPlayerInstance(player),
      "Expected that last player instance updated on spawn");
  }

  @Test
  void testDoesInstanceRemovedOnDisconnect() {
    craftInstancesManager.notifyPlayerDisconnect(spawnInstance, player);

    Assertions.assertNull(
      craftInstancesManager.getLastPlayerInstance(player),
      "Expected that last player instance removed on disconnect");
  }
}
