import { ChallengeItemState } from "../../store/challenge";
import ChallengeItem from "./ChallengeItem";

const ChallengeList: React.FC<{ challenges: ChallengeItemState[] }> = ({
  challenges,
}) => {
  return (
    <div>
      <ul>
        {challenges.map((challenge) => (
          <ChallengeItem key={challenge.id} challenge={challenge} />
        ))}
      </ul>
    </div>
  );
};
export default ChallengeList;
