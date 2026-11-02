"""Overall score. Weights are inputs. The stored row is the configuration."""

from dataclasses import dataclass


@dataclass(frozen=True)
class OverallWeights:
    """Confirmed weights used to publish a score.

    The default values live in `marts.risk_score_config`, not in this type.
    `combine` refuses a row whose status is not `confirmed`.
    """

    environmental: float
    health: float
    social: float
    status: str

    def combine(
        self,
        environmental: float | None,
        health: float | None,
        social: float | None,
    ) -> float | None:
        if self.status != "confirmed":
            raise ValueError("only a confirmed weight row can publish a score")
        if environmental is None or health is None or social is None:
            return None
        return self.environmental * environmental + self.health * health + self.social * social
