package poker.server.model.player;

import javax.ejb.Stateless;

import poker.server.infrastructure.RepositoryGenericJPA;
import poker.server.infrastructure.RepositoryPlayer;

@Stateless
public class RepositoryPlayerJPA extends RepositoryGenericJPA<Player, String>
		implements RepositoryPlayer {

}
