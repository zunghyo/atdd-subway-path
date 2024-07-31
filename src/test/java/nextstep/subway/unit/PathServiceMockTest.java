package nextstep.subway.unit;

import static nextstep.subway.acceptance.station.StationUtils.교대역;
import static nextstep.subway.acceptance.station.StationUtils.신사역;
import static nextstep.subway.acceptance.station.StationUtils.양재역;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.entity.Line;
import nextstep.subway.line.domain.entity.LineSections;
import nextstep.subway.path.application.PathFinder;
import nextstep.subway.path.application.PathService;
import nextstep.subway.path.application.dto.PathResponse;
import nextstep.subway.station.application.dto.StationResponse;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import nextstep.subway.station.exception.StationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PathServiceMockTest {

    @Mock
    private PathFinder pathFinder;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private LineRepository lineRepository;

    @InjectMocks
    private PathService pathService;

    private Station source;
    private Station target;
    private Long sourceId = 1L;
    private Long targetId = 2L;
    private List<Line> lines;

    @BeforeEach
    void setUp() {
        source = new Station(교대역);
        target = new Station(양재역);
        Line 이호선 = new Line("이호선", "bg-red-600", new LineSections());
        Line 신분당선 = new Line("신분당선", "bg-green-600", new LineSections());
        lines = Arrays.asList(이호선, 신분당선);
    }

    @Test
    @DisplayName("유효한 출발역과 도착역 ID가 주어지면 최단 경로를 반환한다")
    void it_returns_shortest_path() {
        // given
        PathResponse expectedPathResponse = new PathResponse(
            Arrays.asList(new StationResponse(sourceId, source.getName()),
                new StationResponse(targetId, target.getName())), 10L);

        when(stationRepository.findByIdOrThrow(sourceId)).thenReturn(source);
        when(stationRepository.findByIdOrThrow(targetId)).thenReturn(target);
        when(lineRepository.findAll()).thenReturn(lines);
        when(pathFinder.find(lines, source, target)).thenReturn(expectedPathResponse);

        // when
        PathResponse actualPathResponse = pathService.findShortestPath(sourceId, targetId);

        // then
        assertThat(actualPathResponse).isEqualTo(expectedPathResponse);
        assertThat(actualPathResponse.getDistance()).isEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 출발역 ID가 주어지면 StationNotFoundException을 던진다")
    void it_throws_StationNotFoundException1() {
        // given
        Long nonExistentSourceId = 999L;

        when(stationRepository.findByIdOrThrow(nonExistentSourceId))
            .thenThrow(new StationNotFoundException(nonExistentSourceId));

        // when, then
        assertThatThrownBy(() -> pathService.findShortestPath(nonExistentSourceId, targetId))
            .isInstanceOf(StationNotFoundException.class)
            .hasMessageContaining(String.valueOf(nonExistentSourceId));
    }

    @Test
    @DisplayName("존재하지 않는 도착역 ID가 주어지면 StationNotFoundException을 던진다")
    void it_throws_StationNotFoundException2() {
        // given
        Long nonExistentTargetId = 999L;

        when(stationRepository.findByIdOrThrow(sourceId)).thenReturn(source);
        when(stationRepository.findByIdOrThrow(nonExistentTargetId))
            .thenThrow(new StationNotFoundException(nonExistentTargetId));

        // when, then
        assertThatThrownBy(() -> pathService.findShortestPath(sourceId, nonExistentTargetId))
            .isInstanceOf(StationNotFoundException.class)
            .hasMessageContaining(String.valueOf(nonExistentTargetId));
    }

}
