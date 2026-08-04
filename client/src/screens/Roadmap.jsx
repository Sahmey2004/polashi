const TOTAL_CHAPTERS = 5;

export default function Roadmap({ chapterResults, animateLatest = false }) {
  const latestChapter = chapterResults.length > 0
    ? chapterResults[chapterResults.length - 1].chapter
    : null;

  return (
    <div className="roadmap">
      {Array.from({ length: TOTAL_CHAPTERS }, (_, i) => {
        const chapterNum = i + 1;
        const result = chapterResults.find((r) => r.chapter === chapterNum);
        const isLatest = animateLatest && chapterNum === latestChapter;

        let cls = "checkpoint";
        if (result) {
          cls += result.winner === "EIC" ? " checkpoint-red" : " checkpoint-green";
          if (isLatest) cls += " checkpoint-pop";
        } else {
          cls += " checkpoint-empty";
        }

        return (
          <div className="checkpoint-wrap" key={chapterNum}>
            <span className={cls}>{chapterNum}</span>
            {chapterNum < TOTAL_CHAPTERS && <span className="checkpoint-connector" />}
          </div>
        );
      })}
    </div>
  );
}
