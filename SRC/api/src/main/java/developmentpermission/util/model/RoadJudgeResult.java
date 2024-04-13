package developmentpermission.util.model;
import java.util.List;

import developmentpermission.entity.SplitLine;
import lombok.Getter;
import lombok.Setter;

/**
 * 道路判定結果モデル
 */
@Getter
@Setter
public class RoadJudgeResult {
	
	/** 道路LOD2レイヤオブジェクトID */
	private Integer lod2ObjectId;
	
	/** 道路LOD2レイヤ路線番号 */
	private String lineNumber;
	
	/** 道路LOD2レイヤ道路種別 */
	private String roadType;
	
	/** 道路部最大幅員 */
	private Double roadMaxWidth;
	
	/** 車道部最大幅員 */
	private Double roadWayMaxWidth;
	
	/** 道路部最小幅員 */
	private Double roadMinWidth;
	
	/** 車道部最小幅員 */
	private Double roadWayMinWidth;
	
	/** 第1方向区割り線取得フラグ */
	private Boolean direction1SplitLineGetFlag;
	
	/** 第2方向区割り線取得フラグ */
	private Boolean direction2SplitLineGetFlag;
	
	/** 判定処理結果フラグ */
	private Boolean judgeProcessResultFlag;
	
	/** 区割り線一覧 */
	private List<SplitLine> splitLineList;
	
	/** 最小幅員区割り線オブジェクトID */
	private Integer minWidthSplitLineObjectId;
	
	/** 最大幅員区割り線オブジェクトID */
	private Integer maxWidthSplitLineObjectId;
	
	/** 隣接歩道フラグ */
	private Boolean adjacentWalkwayFlag;
	
	/** 隣接歩道オブジェクトIDリスト */
	private List<Integer> adjacentWalkwayObjectIdList;
	
	/** 幅員取得エラーフラグ（エラーがある場合false） */
	private Boolean widthErrorFlag;
}
