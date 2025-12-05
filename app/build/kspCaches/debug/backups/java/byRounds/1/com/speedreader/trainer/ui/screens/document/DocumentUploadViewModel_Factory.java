package com.speedreader.trainer.ui.screens.document;

import com.speedreader.trainer.data.repository.DocumentRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DocumentUploadViewModel_Factory implements Factory<DocumentUploadViewModel> {
  private final Provider<DocumentRepository> documentRepositoryProvider;

  public DocumentUploadViewModel_Factory(Provider<DocumentRepository> documentRepositoryProvider) {
    this.documentRepositoryProvider = documentRepositoryProvider;
  }

  @Override
  public DocumentUploadViewModel get() {
    return newInstance(documentRepositoryProvider.get());
  }

  public static DocumentUploadViewModel_Factory create(
      Provider<DocumentRepository> documentRepositoryProvider) {
    return new DocumentUploadViewModel_Factory(documentRepositoryProvider);
  }

  public static DocumentUploadViewModel newInstance(DocumentRepository documentRepository) {
    return new DocumentUploadViewModel(documentRepository);
  }
}
