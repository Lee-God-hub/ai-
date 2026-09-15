package com.smartproperty.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI问答服务类
 * 对接火山方舟大模型（兼容智谱AI，提供智能房产问答功能
 *
 * @author 毕业设计项目
 */
@Service
public class QAService {

    private static final Logger log = LoggerFactory.getLogger(QAService.class);

    @Autowired
    private ZhipuAIClient zhipuAIClient;

    /**
     * 系统提示词 - 房产智能助手角色设定
     */
    private static final String SYSTEM_PROMPT = 
        "你是一个专业的智能房产助手，专注于为用户提供关于买房、租房、房产政策、户型选择、" +
        "贷款、装修、投资、物业等方面的专业建议和解答。\n\n" +
        "回答要求：\n" +
        "1. 回答要专业、准确、实用\n" +
        "2. 语言简洁明了，使用中文回答\n" +
        "3. 适当使用分点列举，方便用户阅读\n" +
        "4. 涉及具体政策时，提醒用户以当地最新政策为准\n" +
        "5. 回答长度控制在200-500字之间\n" +
        "6. 如果问题与房产无关，礼貌引导用户提问房产相关问题";

    /**
     * 问题类型关键词映射
     */
    private static final Map<String, String> QUESTION_TYPE_KEYWORDS = new HashMap<>();

    static {
        QUESTION_TYPE_KEYWORDS.put("buy", "买房|购房|首付|学区房|投资|二手房|新房|购买|买|卖房|房源");
        QUESTION_TYPE_KEYWORDS.put("rent", "租房|租金|押金|合租|房租|退租|被骗|防骗|骗局");
        QUESTION_TYPE_KEYWORDS.put("layout", "户型|朝向|面积|装修|楼层|通风|采光|平米|平方");
        QUESTION_TYPE_KEYWORDS.put("policy", "政策|限购|税费|公积金|贷款|利率|契税|首付");
        QUESTION_TYPE_KEYWORDS.put("decoration", "装修|精装|毛坯|简装|翻新|装饰");
        QUESTION_TYPE_KEYWORDS.put("location", "地段|位置|交通|地铁|配套|周边");
        QUESTION_TYPE_KEYWORDS.put("investment", "投资|升值|保值|回报率|涨价");
        QUESTION_TYPE_KEYWORDS.put("loan", "贷款|房贷|按揭|月供|还款|利率");
        QUESTION_TYPE_KEYWORDS.put("property_management", "物业|物业费|维修|保安|物业纠纷");
        QUESTION_TYPE_KEYWORDS.put("general", "你好|谢谢|推荐|建议|咨询|房产|房子|中介");
    }

    /**
     * 根据问题获取AI答案
     * 优先级：外部 AI 大模型 > 本地知识库 > 通用降级回答
     * 注：配置了有效 API key 时优先调用 AI，确保回答质量；
     *     未配置或 AI 调用失败时，自动降级到本地知识库
     *
     * @param question 用户问题
     * @return AI答案
     */
    public String getAnswer(String question) {
        log.info("收到问题: {}", question);

        // 1. 有有效 API key → 优先调用外部 AI 大模型
        boolean hasValidApiKey = zhipuAIClient.hasValidApiKey();
        if (hasValidApiKey) {
            try {
                String answer = zhipuAIClient.chat(SYSTEM_PROMPT, question);
                if (answer != null && !answer.trim().isEmpty()) {
                    log.info("外部 AI 回答成功，长度: {}", answer.length());
                    return answer;
                }
            } catch (Exception e) {
                log.warn("外部 AI 调用失败，降级使用本地知识库回答，原因: {}",
                        e.getMessage());
            }
        } else {
            log.info("未配置有效外部 AI key，使用本地知识库回答");
        }

        // 2. 降级：使用本地知识库（关键词匹配的专业问答模板）
        String localAnswer = getLocalAnswer(question);
        if (localAnswer != null && !localAnswer.isEmpty()) {
            log.info("本地知识库回答，长度: {}", localAnswer.length());
            return localAnswer;
        }

        // 3. 最终降级：通用回答
        return getFallbackAnswer(question);
    }

    /**
     * 本地知识库回答（基于问题类型关键词）
     */
    private String getLocalAnswer(String question) {
        String type = identifyQuestionType(question);
        StringBuilder answer = new StringBuilder();
        answer.append("关于您的问题，以下是专业建议：\n\n");

        switch (type) {
            case "buy":
                answer.append("【买房指南】\n")
                      .append("1. 预算评估：首付比例通常为30%-50%，月供不宜超过月收入的50%\n")
                      .append("2. 地段选择：优先考虑地铁口、学校、医院、商圈等配套\n")
                      .append("3. 产权核实：确认房产证、土地证齐全，注意房屋年限\n")
                      .append("4. 价格谈判：对比同区域同户型成交价，理性议价\n")
                      .append("5. 贷款方式：公积金贷款利率更低，组合贷是常见方案");
                break;
            case "rent":
                answer.append("【租房指南】\n")
                      .append("1. 房源选择：优先地铁沿线小区，通勤时间控制在45分钟内\n")
                      .append("2. 合同签订：务必查看房东身份证、房产证原件，明确租期与退租条款\n")
                      .append("3. 押金条款：通常为1-2个月租金，退房无损坏应全额退还\n")
                      .append("4. 费用确认：水电煤物业费由谁承担，需在合同中明确\n")
                      .append("5. 警惕陷阱：拒绝提前支付大额费用，避免二房东骗局");
                break;
            case "layout":
                answer.append("【户型与装修】\n")
                      .append("1. 户型方正：南北通透、明厨明卫为佳，避免异形空间\n")
                      .append("2. 朝向选择：南>东南>东>西南>北，采光与通风并重\n")
                      .append("3. 面积利用：实际使用面积>建筑面积，避免公摊过高\n")
                      .append("4. 楼层选择：中高楼层采光好，低楼层出行方便但需注意潮湿\n")
                      .append("5. 装修评估：精装房需仔细验房，毛坯可按需定制装修风格");
                break;
            case "policy":
                answer.append("【政策与税费】\n")
                      .append("1. 限购政策：不同城市限制不同，购房前需确认购房资格\n")
                      .append("2. 主要税费：契税（1%-3%）、增值税（满2年免征）、个税（满5唯一免征）\n")
                      .append("3. 公积金贷款：利率通常比商贷低1-2个百分点，首选使用\n")
                      .append("4. 首套认定：不同城市首套认定标准不同，影响首付比例和贷款利率\n")
                      .append("5. 政策变动：购房前请以当地最新官方政策为准");
                break;
            case "loan":
                answer.append("【贷款与还款】\n")
                      .append("1. 贷款方式：商贷利率较高但额度大，公积金利率低但有额度上限\n")
                      .append("2. 还款方式：等额本金前期压力大、总利息少；等额本息月供稳定\n")
                      .append("3. 贷款年限：最长可贷30年，借款人年龄+贷款年限一般不超70年\n")
                      .append("4. 提前还款：若利率较高或手头有闲置资金可考虑，但需注意违约金\n")
                      .append("5. 月供控制：建议月供不超过家庭月收入的40%，预留应急资金");
                break;
            case "decoration":
                answer.append("【装修建议】\n")
                      .append("1. 精装vs毛坯：精装省时省心但风格固定，毛坯可按需定制\n")
                      .append("2. 预算规划：硬装占60%，软装30%，预留10%应急\n")
                      .append("3. 水电改造：优先规划，避免完工后返工\n")
                      .append("4. 入住时间：装修后建议通风3-6个月再入住，尤其有儿童和孕妇\n")
                      .append("5. 环保材料：优先选择E0/E1级板材、水性漆");
                break;
            case "location":
                answer.append("【地段与配套】\n")
                      .append("1. 交通：距离地铁站500米内最优，1公里内可接受\n")
                      .append("2. 学区：学区房溢价明显，需结合预算和家庭实际需求\n")
                      .append("3. 商业：3公里内有大型商圈，生活便利性高\n")
                      .append("4. 医疗：三甲医院距离建议在5-10公里范围内\n")
                      .append("5. 升值潜力：关注区域规划，地铁、商业、产业迁入是利好信号");
                break;
            case "investment":
                answer.append("【投资建议】\n")
                      .append("1. 区位优先：一线城市核心区域>二线城市>三四线城市\n")
                      .append("2. 回报率：住宅租赁回报率约2%-3%，商业地产5%-8%不等\n")
                      .append("3. 持有成本：物业费、税费、装修维护需计算在投资成本内\n")
                      .append("4. 政策风险：房地产税、限售限贷等政策可能影响投资收益\n")
                      .append("5. 分散投资：不建议全部资金投入房产，注意资产配置平衡");
                break;
            case "property_management":
                answer.append("【物业服务】\n")
                      .append("1. 物业费：通常每平米2-8元/月不等，高档住宅更高\n")
                      .append("2. 服务内容：安保、保洁、绿化、公共设施维修等\n")
                      .append("3. 选择标准：品牌物业>本地知名物业>不知名小物业\n")
                      .append("4. 维权渠道：业主委员会>街道办>住建局>法律途径\n")
                      .append("5. 注意事项：入住前验收房屋质量，留存证据");
                break;
            case "general":
                answer.append("【智能房产助手】\n")
                      .append("您好！我可以为您解答以下方面的问题：\n")
                      .append("1. 买房：首付计算、购房资格、房源选择、合同签订注意事项\n")
                      .append("2. 租房：租金、押金、签合同、选择合租、退租维权\n")
                      .append("3. 户型：朝向、楼层、采光通风、面积利用\n")
                      .append("4. 政策：限购、税费、公积金、贷款利率\n")
                      .append("5. 贷款：商贷、公积金贷、提前还款、还款方式\n")
                      .append("6. 投资：回报率分析、地段评估、升值潜力\n")
                      .append("7. 物业：物业费标准、物业维权、物业服务\n\n")
                      .append("请您明确问题类型或直接提问，我会为您提供专业建议！");
                break;
            default:
                return null; // 本地知识库无法回答
        }

        answer.append("\n\n以上建议仅供参考，具体决策建议咨询专业人士或关注最新本地政策。");
        return answer.toString();
    }

    /**
     * 识别问题类型
     *
     * @param question 用户问题
     * @return 问题类型
     */
    public String identifyQuestionType(String question) {
        for (Map.Entry<String, String> entry : QUESTION_TYPE_KEYWORDS.entrySet()) {
            String type = entry.getKey();
            String keywords = entry.getValue();
            String[] keywordArray = keywords.split("\\|");
            
            for (String keyword : keywordArray) {
                if (question.contains(keyword)) {
                    return type;
                }
            }
        }
        return "general";
    }

    /**
     * 获取相关问题推荐
     *
     * @param questionType 问题类型
     * @return 相关问题列表
     */
    public List<String> getRelatedQuestions(String questionType) {
        Map<String, List<String>> relatedQuestionsMap = new HashMap<>();
        
        relatedQuestionsMap.put("buy", Arrays.asList(
            "买房需要准备多少首付？",
            "如何选择合适的地段？",
            "学区房值得投资吗？"
        ));
        
        relatedQuestionsMap.put("rent", Arrays.asList(
            "租房押金一般是多少？",
            "如何避免租房陷阱？",
            "合租需要注意什么？"
        ));
        
        relatedQuestionsMap.put("layout", Arrays.asList(
            "什么样的户型最好？",
            "房屋朝向怎么选？",
            "如何判断房屋采光？"
        ));
        
        relatedQuestionsMap.put("policy", Arrays.asList(
            "首套房有什么优惠政策？",
            "公积金贷款怎么申请？",
            "买房需要交哪些税？"
        ));

        relatedQuestionsMap.put("decoration", Arrays.asList(
            "精装房和毛坯房哪个好？",
            "装修预算一般多少合适？",
            "新房多久可以入住？"
        ));

        relatedQuestionsMap.put("location", Arrays.asList(
            "地铁房有什么优势？",
            "如何判断地段的升值潜力？",
            "学区和交通哪个更重要？"
        ));

        relatedQuestionsMap.put("investment", Arrays.asList(
            "现在适合投资房产吗？",
            "哪种房产投资回报率高？",
            "买房投资需要注意什么？"
        ));

        relatedQuestionsMap.put("loan", Arrays.asList(
            "等额本金和等额本息哪个好？",
            "提前还贷划算吗？",
            "房贷最长可以贷多少年？"
        ));

        relatedQuestionsMap.put("property_management", Arrays.asList(
            "物业费包含哪些服务？",
            "如何判断物业好不好？",
            "物业纠纷怎么解决？"
        ));
        
        relatedQuestionsMap.put("general", Arrays.asList(
            "如何选择靠谱的房产中介？",
            "物业费包含哪些服务？",
            "买房和租房哪个更划算？"
        ));
        
        return relatedQuestionsMap.getOrDefault(questionType, 
            Arrays.asList("买房需要注意什么？", "如何选择合适的房源？", "贷款政策有哪些？"));
    }

    /**
     * 降级回答（当AI服务不可用时）
     *
     * @param question 用户问题
     * @return 降级答案
     */
    private String getFallbackAnswer(String question) {
        return "感谢您的提问！关于\"" + question + "\"这个问题，我建议您：\n\n" +
               "1. 咨询专业的房产中介或置业顾问，他们能提供更详细的建议\n" +
               "2. 到当地房管局或相关部门了解最新政策\n" +
               "3. 实地考察多个房源，综合对比后做决定\n" +
               "4. 关注官方房产平台的信息和指南\n\n" +
               "如果您有其他关于买房、租房、户型、政策等方面的问题，欢迎继续提问！\n\n" +
               "（注：AI服务暂时繁忙，以上为预设回答，请稍后重试获取更精准的回答）";
    }
}
