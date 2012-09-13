package edu.ntnu.texasai.controller;

import edu.ntnu.texasai.model.BettingDecision;
import edu.ntnu.texasai.model.GameHand;
import edu.ntnu.texasai.model.Player;
import edu.ntnu.texasai.model.cards.Card;
import edu.ntnu.texasai.model.cards.EquivalenceClass;
import edu.ntnu.texasai.persistence.PersistenceManager;
import edu.ntnu.texasai.utils.Logger;

import javax.inject.Inject;
import java.util.List;

public class PlayerControllerPhaseII extends PlayerController {
    private final PlayerControllerPhaseI playerControllerPhaseI;
    private final PersistenceManager persistanceController;
    private final EquivalenceClassController equivalenceClassController;
    private final HandStrengthEvaluator handStrengthEvaluator;
    private final Logger logger;

    @Inject
    public PlayerControllerPhaseII(final PlayerControllerPhaseI playerControllerPhaseI,
                                   final EquivalenceClassController equivalenceClassController,
                                   final PersistenceManager persistanceController,
                                   final HandStrengthEvaluator handStrengthEvaluator,
                                   final Logger logger) {
        this.playerControllerPhaseI = playerControllerPhaseI;
        this.persistanceController = persistanceController;
        this.equivalenceClassController = equivalenceClassController;
        this.handStrengthEvaluator = handStrengthEvaluator;
        this.logger = logger;
    }

    @Override
    public String toString() {
        return "PhaseII";
    }

    @Override
    public BettingDecision decidePreFlop(Player player, GameHand gameHand, List<Card> cards) {
        Card card1 = cards.get(0);
        Card card2 = cards.get(1);
        EquivalenceClass equivalenceClass = this.equivalenceClassController.cards2Equivalence(card1, card2);
        Double percentageOfWins = this.persistanceController.retrievePercentageOfWinsByPlayerAndEquivalenceClass(
                gameHand.getPlayers().size(), equivalenceClass);

        if (percentageOfWins > 0.8)
            return BettingDecision.RAISE;
        else if (percentageOfWins < 0.5)
            return BettingDecision.FOLD;
        return BettingDecision.CALL;
    }

    @Override
    public BettingDecision decideAfterFlop(Player player, GameHand gameHand, List<Card> cards) {
        Double handStrength = this.handStrengthEvaluator.evaluate(player.getHoleCards(), gameHand.getSharedCards(),
                gameHand.getPlayers().size());
        if (handStrength > 0.8) {
            return BettingDecision.RAISE;
        } else if (handStrength > 0.4 || canCheck(gameHand, player)) {
            return BettingDecision.CALL;
        }
        return BettingDecision.FOLD;
    }
}
