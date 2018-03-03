package com.zee.webgame.mapper;

import java.util.List;
import java.util.Map;

import com.zee.webgame.model.Question;
import com.zee.webgame.model.SMS;

public interface QuestionMapper {

	List<Question> getQuestionList(Integer number);
}
