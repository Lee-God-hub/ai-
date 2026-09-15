package com.smartproperty.service;

import com.smartproperty.dto.ContentGenerationResponse;
import com.smartproperty.dto.ContentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * AI文案生成服务类
 * 对接火山方舟大模型（兼容智谱AI，自动生成房源宣传文案
 *
 * @author 毕业设计项目
 */
@Service
public class ContentGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ContentGenerationService.class);

    @Autowired
    private ZhipuAIClient zhipuAIClient;

    /**
     * 房源类型名称映射
     */
    private static final String[] PROPERTY_TYPE_NAMES = {"住宅", "公寓", "别墅"};

    /**
     * 交易类型名称映射
     */
    private static final String[] TRANSACTION_TYPE_NAMES = {"出租", "出售"};

    /**
     * 生成房源文案
     * 优先调用 AI 大模型，失败时自动使用本地智能模板
     */
    public ContentGenerationResponse generate(ContentRequest request) {
        log.info("开始生成文案，房源标题: {}", request.getTitle());

        long startTime = System.currentTimeMillis();

        boolean useAIClient = zhipuAIClient.hasValidApiKey();
        if (useAIClient) {
            log.info("检测到有效 API key，优先调用 AI 大模型生成");
        } else {
            log.info("未配置有效 AI API key，使用本地智能模板生成");
        }

        String propertyInfo = buildPropertyInfo(request);

        String shortContent;
        String detailContent;

        if (useAIClient) {
            try {
                shortContent = generateShortContent(propertyInfo, request);
            } catch (Exception e) {
                log.warn("AI 生成简短文案失败，使用本地模板: {}", e.getMessage());
                shortContent = generateFallbackShortContent(request);
            }
            try {
                detailContent = generateDetailContent(propertyInfo, request);
            } catch (Exception e) {
                log.warn("AI 生成详细文案失败，使用本地模板: {}", e.getMessage());
                detailContent = generateFallbackDetailContent(request);
            }
        } else {
            shortContent = generateFallbackShortContent(request);
            detailContent = generateFallbackDetailContent(request);
        }

        long endTime = System.currentTimeMillis();
        long generationTime = endTime - startTime;

        log.info("文案生成完成，耗时: {}ms，简短版: {}字，详细版: {}字",
                generationTime, shortContent.length(), detailContent.length());

        ContentGenerationResponse response = new ContentGenerationResponse(shortContent, detailContent);
        response.setGenerationTime(generationTime);

        return response;
    }

    /**
     * 构建房源信息描述（作为AI输入上下文）
     */
    private String buildPropertyInfo(ContentRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("房源标题：").append(request.getTitle()).append("\n");
        sb.append("房源类型：").append(getPropertyTypeName(request.getPropertyType())).append("\n");
        sb.append("交易类型：").append(getTransactionTypeName(request.getTransactionType())).append("\n");
        sb.append("面积：").append(request.getArea()).append("㎡\n");
        sb.append("价格：").append(formatPrice(request.getPrice()));
        if (request.getPriceType() != null && request.getPriceType() == 0) {
            sb.append("元/月\n");
        } else {
            sb.append("万元\n");
        }
        sb.append("城市：").append(request.getCity()).append("\n");
        
        if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
            sb.append("区域：").append(request.getDistrict()).append("\n");
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            sb.append("地址：").append(request.getAddress()).append("\n");
        }
        if (request.getBedrooms() != null && request.getBathrooms() != null) {
            sb.append("户型：").append(request.getBedrooms()).append("室").append(request.getBathrooms()).append("卫\n");
        }
        if (request.getOrientation() != null && !request.getOrientation().isEmpty()) {
            sb.append("朝向：").append(request.getOrientation()).append("\n");
        }
        if (request.getFloor() != null && !request.getFloor().isEmpty()) {
            sb.append("楼层：").append(request.getFloor()).append("\n");
        }
        if (request.getDecoration() != null && !request.getDecoration().isEmpty()) {
            sb.append("装修：").append(request.getDecoration()).append("\n");
        }
        if (request.getFacilities() != null && !request.getFacilities().isEmpty()) {
            sb.append("周边配套：").append(request.getFacilities()).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * 使用AI生成简短版文案（100-200字）
     */
    private String generateShortContent(String propertyInfo, ContentRequest request) {
        String prompt = "请根据以下房源信息，生成一段100-200字的简短房源宣传文案。" +
                "要求：简洁有吸引力，突出核心卖点，适合在列表中展示。" +
                "直接输出文案内容，不要加任何前缀说明。\n\n" +
                "房源信息：\n" + propertyInfo;

        String systemPrompt = "你是一个专业的房地产文案撰写专家，善于撰写有吸引力的房源宣传文案。";

        try {
            String result = zhipuAIClient.chat(systemPrompt, prompt);
            log.info("AI生成简短文案成功，长度: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("AI生成简短文案失败，使用降级模板", e);
            return generateFallbackShortContent(request);
        }
    }

    /**
     * 使用AI生成详细版文案（300-500字）
     */
    private String generateDetailContent(String propertyInfo, ContentRequest request) {
        String prompt = "请根据以下房源信息，生成一段300-500字的详细房源宣传文案。" +
                "要求：\n" +
                "1. 包含房源概况、位置交通、配套设施、房源亮点等板块\n" +
                "2. 使用适当的emoji图标美化排版\n" +
                "3. 语言专业、有感染力\n" +
                "4. 适合在房源详情页展示\n" +
                "直接输出文案内容，不要加任何前缀说明。\n\n" +
                "房源信息：\n" + propertyInfo;

        String systemPrompt = "你是一个专业的房地产文案撰写专家，善于撰写详尽且有吸引力的房源详情描述。";

        try {
            String result = zhipuAIClient.chat(systemPrompt, prompt);
            log.info("AI生成详细文案成功，长度: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("AI生成详细文案失败，使用降级模板", e);
            return generateFallbackDetailContent(request);
        }
    }

    /**
     * 简短版智能模板（AI不可用时）
     */
    private String generateFallbackShortContent(ContentRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(request.getTitle()).append("】\n\n");
        sb.append("✨ ").append(request.getCity());
        if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
            sb.append("·").append(request.getDistrict());
        }
        sb.append("优质").append(getPropertyTypeName(request.getPropertyType())).append("源推荐！\n\n");

        sb.append("🏠 ").append(getPropertyTypeName(request.getPropertyType()));
        if (request.getBedrooms() != null && request.getBathrooms() != null) {
            sb.append("｜").append(request.getBedrooms()).append("室").append(request.getBathrooms()).append("卫");
        }
        sb.append("｜建筑面积约 ").append(request.getArea()).append("㎡\n");

        sb.append("💰 ").append(getTransactionTypeName(request.getTransactionType())).append("价格：")
                .append(formatPrice(request.getPrice()));
        if (request.getPriceType() != null && request.getPriceType() == 0) {
            sb.append("元/月");
        } else {
            sb.append("万元");
        }
        sb.append("\n");

        if (request.getOrientation() != null && !request.getOrientation().isEmpty()) {
            sb.append("☀️ 朝向：").append(request.getOrientation()).append("，采光充足\n");
        }
        if (request.getDecoration() != null && !request.getDecoration().isEmpty()) {
            sb.append("🎨 装修：").append(request.getDecoration()).append("\n");
        }
        if (request.getFacilities() != null && !request.getFacilities().isEmpty()) {
            sb.append("🛍️ 配套：").append(request.getFacilities()).append("\n");
        }

        sb.append("\n🔥 房源亮点：交通便利、配套成熟、户型方正，").append(request.getCity())
                .append("置业首选！欢迎实地看房～");
        return sb.toString();
    }

    /**
     * 详细版智能模板（AI不可用时）
     */
    private String generateFallbackDetailContent(ContentRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("【").append(request.getTitle()).append("】\n\n");

        sb.append("═══ 🏠 房源概况 ═══\n");
        sb.append("• 房源类型：").append(getPropertyTypeName(request.getPropertyType())).append("\n");
        sb.append("• 建筑面积：").append(request.getArea()).append("㎡\n");
        if (request.getBedrooms() != null && request.getBathrooms() != null) {
            sb.append("• 户型配置：").append(request.getBedrooms()).append("室").append(request.getBathrooms()).append("卫\n");
        }
        if (request.getFloor() != null && !request.getFloor().isEmpty()) {
            sb.append("• 楼层：").append(request.getFloor()).append("\n");
        }
        if (request.getOrientation() != null && !request.getOrientation().isEmpty()) {
            sb.append("• 朝向：").append(request.getOrientation()).append("\n");
        }
        if (request.getDecoration() != null && !request.getDecoration().isEmpty()) {
            sb.append("• 装修情况：").append(request.getDecoration()).append("\n");
        }
        sb.append("\n");

        sb.append("═══ 📍 位置交通 ═══\n");
        sb.append("• 所在城市：").append(request.getCity());
        if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
            sb.append(" · ").append(request.getDistrict());
        }
        sb.append("\n");
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            sb.append("• 详细地址：").append(request.getAddress()).append("\n");
        }
        sb.append("• 交通便利：周边路网发达，出行便捷\n\n");

        sb.append("═══ 🛍️ 周边配套 ═══\n");
        if (request.getFacilities() != null && !request.getFacilities().isEmpty()) {
            sb.append("• 生活配套：").append(request.getFacilities()).append("\n");
        }
        sb.append("• 教育资源：周边学校林立，教育氛围浓厚\n");
        sb.append("• 医疗保障：大型医院临近，健康有保障\n\n");

        sb.append("═══ 💰 价格信息 ═══\n");
        sb.append("• ").append(getTransactionTypeName(request.getTransactionType())).append("价格：")
                .append(formatPrice(request.getPrice()));
        if (request.getPriceType() != null && request.getPriceType() == 0) {
            sb.append("元/月");
        } else {
            sb.append("万元");
        }
        sb.append("\n");
        sb.append("• 性价比分析：同地段同类房源中价格合理，值得关注\n\n");

        sb.append("═══ ✨ 核心亮点 ═══\n");
        sb.append("★ 地段优越：").append(request.getCity()).append("核心区域，地段价值凸显\n");
        sb.append("★ 户型方正：空间利用率高，居住舒适度好\n");
        sb.append("★ 采光通风：").append(request.getOrientation() != null ? request.getOrientation() : "南北通透")
                .append("，阳光充足，空气流通\n");
        sb.append("★ 配套成熟：周边生活配套一应俱全，生活便利\n\n");

        sb.append("💡 温馨提示：房源信息真实有效，建议实地看房感受。").append(request.getCity())
                .append("房产市场活跃，优质房源抢手，有意向请及时联系！\n\n");
        sb.append("—— 本文案由智能推荐系统生成，仅供参考 ——");

        return sb.toString();
    }

    private String getPropertyTypeName(int type) {
        if (type >= 0 && type < PROPERTY_TYPE_NAMES.length) {
            return PROPERTY_TYPE_NAMES[type];
        }
        return "其他";
    }

    private String getTransactionTypeName(int type) {
        if (type >= 0 && type < TRANSACTION_TYPE_NAMES.length) {
            return TRANSACTION_TYPE_NAMES[type];
        }
        return "其他";
    }

    /**
     * 格式化价格
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) return "面议";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(price);
    }
}
