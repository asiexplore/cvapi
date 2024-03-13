package com.hackathon.cvapi.service;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.PdfDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.vertexai.VertexAiChatModel;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.stereotype.Service;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Service
public class VertexAIService {

    public static final String GOOGLE_APIS_ENDPOINT = "us-central1-aiplatform.googleapis.com:443";
    public static final String PROJECT_ID = "dotted-transit-416516";
    public static final String LOCATION = "us-central1";
    public static final String PUBLISHER = "google";
    private VertexAiEmbeddingModel embeddingModel;

    private EmbeddingStoreIngestor storeIngestor;

    InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    EmbeddingStoreRetriever retriever;

    VertexAiChatModel model;

    ConversationalRetrievalChain rag;

    public VertexAIService() {
        embeddingModel = VertexAiEmbeddingModel.builder()
                                            .endpoint(GOOGLE_APIS_ENDPOINT)
                                            .project(PROJECT_ID)
                                            .location(LOCATION)
                                            .publisher(PUBLISHER)
                                            .modelName("textembedding-gecko@001")
                                            .maxRetries(3)
                                            .build();

        storeIngestor = EmbeddingStoreIngestor.builder()
                                            .documentSplitter(DocumentSplitters.recursive(500, 100))
                                            .embeddingModel(embeddingModel)
                                            .embeddingStore(embeddingStore)
                                            .build();

        retriever = EmbeddingStoreRetriever.from(embeddingStore, embeddingModel);

        model = VertexAiChatModel.builder()
                                .endpoint(GOOGLE_APIS_ENDPOINT)
                                .project(PROJECT_ID)
                                .location(LOCATION)
                                .publisher(PUBLISHER)
                                .modelName("chat-bison@001")
                                .maxOutputTokens(1000)
                                .build();

        rag = ConversationalRetrievalChain.builder()
                                        .chatLanguageModel(model)
                                        .retriever(retriever)
                                        .promptTemplate(PromptTemplate.from("""
                                                        Answer to the following query the best as you can: {{question}}
                                                        Base your answer on the information provided below:
                                                        {{information}}
                                                        """
                                                        ))
                                        .build();
    }

    public void storeDoc(MultipartFile docToStore) throws IOException {
        PdfDocumentParser pdfParser = new PdfDocumentParser();
        Document document = pdfParser.parse(docToStore.getInputStream());
        storeIngestor.ingest(document);
    }


    public String searchDocs(String str) {
        String strResp = rag.execute(str);
        return strResp;
    }
}
